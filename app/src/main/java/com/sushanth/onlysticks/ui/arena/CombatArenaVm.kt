// ViewModel that runs a duel against a bot (PvE).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena

import android.util.Log
import androidx.lifecycle.ViewModel
import com.sushanth.onlysticks.BuildConfig
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.data.repository.MatchOutcome
import com.sushanth.onlysticks.data.repository.MatchLifecycleSink
import com.sushanth.onlysticks.domain.combat.CombatEvt
import com.sushanth.onlysticks.domain.combat.CombatEngine
import com.sushanth.onlysticks.domain.combat.CombatState
import com.sushanth.onlysticks.domain.combat.PlrId
import com.sushanth.onlysticks.domain.combat.TurnTmr
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Deck
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpTarget
import com.sushanth.onlysticks.domain.model.TurnAction
import com.sushanth.onlysticks.domain.pve.BotBrain
import com.sushanth.onlysticks.domain.pve.BotDeckGenerator
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import com.sushanth.onlysticks.domain.rng.Hash
import com.sushanth.onlysticks.domain.rng.Hash.toHex
import com.sushanth.onlysticks.domain.rng.SharedEntropy
import com.sushanth.onlysticks.domain.trainer.TrainerProgression
import com.sushanth.onlysticks.ui.common.UiTxt
import com.sushanth.onlysticks.ui.common.uiTxt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** UI state for one round of a match: the combat snapshot plus what the player has selected so far. */
data class MatchUi(
    val combat: CombatState,
    val legalMoves: Set<Move>,
    val selMove: Move? = null,
    val selSwtchSlot: Int? = null,
    val selPwrUpSlot: Int? = null,
    val selPwrUpTarget: PwrUpTarget? = null,
    val switchableSlots: List<Int> = emptyList(),
    val pwrUpSlots: List<Int> = emptyList(),
    val oppReady: Boolean = false,
    val waitForOpp: Boolean = false,
) {
    /** The winner once the match is over. */
    val winner: PlrId? get() = combat.winner
    val isOver: Boolean get() = combat.isOver
    /** The submit button is enabled once a move or power-up is picked and we're not waiting on the opponent. */
    val canSbmt: Boolean get() = (selMove != null || selPwrUpSlot != null) && !waitForOpp

    /** Builds the [TurnAction] to submit from the current selection, or null if nothing is picked. */
    fun action(): TurnAction? = when {
        selPwrUpSlot != null -> TurnAction(null, selSwtchSlot, selPwrUpSlot, selPwrUpTarget)
        selMove != null -> TurnAction(selMove, selSwtchSlot)
        else -> null
    }

    /** Picks a move (and cancels any selected power-up, since items use the whole turn). */
    fun withMove(move: Move): MatchUi = copy(selMove = move, selPwrUpSlot = null, selPwrUpTarget = null)

    /** Picks or clears a power-up, preselecting its default target and clearing the move. */
    fun withPwrUp(slot: Int?): MatchUi {
        val type = slot?.let { combat.p1.pwrUpAt(it) }
        return copy(
            selPwrUpSlot = slot,
            selPwrUpTarget = type?.let(PwrUpTarget::defFor),
            selMove = if (slot != null) null else selMove,
        )
    }

    companion object {
        /** Fresh selection state for a new round. */
        fun forRound(combat: CombatState): MatchUi {
            val plr = combat.p1
            return MatchUi(
                combat = combat,
                legalMoves = CombatEngine.legalMoves(plr),
                switchableSlots = plr.sticks.filter { (slot, s) -> !s.brkn && slot != plr.activeSlot }.keys.sorted(),
                pwrUpSlots = plr.deck.indices.filter { plr.pwrUpAt(it) != null },
            )
        }
    }
}

/** Events from the last resolved round, for the stage animation. [seq] increases so the same events replay only once. */
data class RoundPb(val seq: Int, val evts: List<CombatEvt>)

/** Full arena screen state: chosen bot tier, current match, battle log and notices. */
data class ArenaUiState(
    val botTier: Int = 1,
    val match: MatchUi? = null,
    val log: List<UiTxt> = emptyList(),
    val notice: UiTxt? = null,
    val lastRound: RoundPb? = null,
    val opp: OppLbl = OppLbl.Bot(botTier),
    val canRematch: Boolean = true,
)

/**
 * Runs bot matches locally. The match seed comes from two random nonces; the bot's next action is picked
 * at the start of each round and revealed after its random "thinking" delay passes on the turn timer.
 * All constructor parameters have defaults and exist so tests can inject fixed values.
 */
class CombatArenaVm(
    private val nonceSrc: () -> ByteArray = { SharedEntropy.newNonce() },
    private val turnDurMs: Long = Cfg.TURN_TIMER_MS,
    private val botDeckFor: (tier: Int, seed: ByteArray) -> List<Card> = ::defBotDeck,
    private val matchSink: MatchLifecycleSink = MatchLifecycleSink.NONE,
) : ViewModel(), DuelCtrl {
    override val state: StateFlow<ArenaUiState> get() = _state.asStateFlow()
    private val _state = MutableStateFlow(ArenaUiState())
    private val _tmr = MutableStateFlow(TurnTmr(turnDurMs))
    override val tmr: StateFlow<TurnTmr> = _tmr.asStateFlow()
    private var combatRng: DeterministicRng? = null
    private var botRng: DeterministicRng? = null
    private var botAction: TurnAction? = null
    private var botDelayMs: Long = 0
    private var equippedTrnrId: String? = null
    private var matchId: String = ""
    /** Changes the bot difficulty (not allowed mid-match). */
    fun selectTier(tier: Int) {
        if (tier !in 1..Cfg.Bots.TIERS || _state.value.match?.isOver == false) return
        _state.update { it.copy(botTier = tier, opp = OppLbl.Bot(tier)) }
    }

    /** Starts a match against a bot of the selected tier using [plrDeck]. Returns false if a match is already running. */
    fun startMatch(plrDeck: Deck): Boolean {
        if (activeMatch() != null) return false
        val tier = _state.value.botTier
        val seed = SharedEntropy.shrdSeed(nonceSrc(), nonceSrc())
        matchId = seed.toHex().take(32)
        combatRng = DeterministicRng.fromBytes(seed)
        botRng = BotBrain.rngFor(seed)
        equippedTrnrId = plrDeck.trainer?.id
        val combat = CombatEngine.start(
            deck1 = plrDeck.cardList,
            deck2 = botDeckFor(tier, seed),
            modifiers1 = TrainerProgression.modifiersFor(plrDeck.trainer),
        )
        _tmr.value = TurnTmr(turnDurMs)
        _state.update {
            it.copy(match = beginRound(combat), log = emptyList(), notice = null, lastRound = null)
        }
        matchSink.onMatchStarted(matchId, tier, equippedTrnrId)
        return true
    }
    /** Clears a finished match to go back to the lobby. */
    override fun leaveMatch() {
        if (_state.value.match?.isOver != true) return
        _state.update { it.copy(match = null, log = emptyList(), notice = null, lastRound = null) }
    }
    /** Selects a move; shows a notice if it's not allowed right now. */
    override fun selectMove(move: Move): Boolean {
        val match = activeMatch() ?: return false
        if (move !in match.legalMoves) {
            _state.update { it.copy(notice = uiTxt(R.string.arena_notice_illegal_move)) }
            return false
        }
        updateMatch(match.withMove(move), clearNotice = true)
        return true
    }
    /** Selects a stick to switch to before acting (null cancels the switch). */
    override fun selectSwtch(slot: Int?): Boolean {
        val match = activeMatch() ?: return false
        if (slot != null && slot !in match.switchableSlots) {
            _state.update { it.copy(notice = uiTxt(R.string.arena_notice_illegal_swtch)) }
            return false
        }
        updateMatch(match.copy(selSwtchSlot = slot), clearNotice = true)
        return true
    }
    /** Selects a power-up to use this turn (null cancels). */
    override fun selectPwrUp(slot: Int?): Boolean {
        val match = activeMatch() ?: return false
        if (slot != null && slot !in match.pwrUpSlots) {
            _state.update { it.copy(notice = uiTxt(R.string.arena_notice_illegal_powerup)) }
            return false
        }
        updateMatch(match.withPwrUp(slot), clearNotice = true)
        return true
    }
    /** Chooses who the selected power-up targets. */
    override fun selectPwrUpTarget(target: PwrUpTarget): Boolean {
        val match = activeMatch()?.takeIf { it.selPwrUpSlot != null } ?: return false
        updateMatch(match.copy(selPwrUpTarget = target), clearNotice = true)
        return true
    }
    /** Submits the player's action and resolves the round. */
    override fun sbmt(): Boolean {
        val match = activeMatch() ?: return false
        val action = match.action()
        if (action == null || _tmr.value.isDone) {
            _state.update { it.copy(notice = uiTxt(R.string.arena_notice_pick_move)) }
            return false
        }
        resolve(match, action, _tmr.value.sbmt())
        return true
    }
    /** Convenience: select and submit in one call. */
    fun sbmtMove(move: Move): Boolean = selectMove(move) && sbmt()
    /** Advances the turn timer. Auto-skips the player's turn if time runs out; shows "opponent ready" after the bot's delay. */
    override fun onTmrTick(elapsedMs: Long) {
        val match = activeMatch() ?: return
        val (next, evt) = _tmr.value.tick(elapsedMs)
        if (evt == TurnTmr.Event.AutoSkip) {
            resolve(match, TurnAction.AUTO_SKIP, next)
        } else {
            _tmr.value = next
            val ready = next.durMs - next.leftMs >= botDelayMs
            if (ready != match.oppReady) updateMatch(match.copy(oppReady = ready))
        }
    }
    override fun pauseTmr() {
        if (activeMatch() == null) return
        _tmr.value = _tmr.value.pause()
    }

    override fun resumeTmr() {
        if (activeMatch() == null) return
        _tmr.value = _tmr.value.resume()
    }

    /** The current match if it is still being played. */
    private fun activeMatch(): MatchUi? = _state.value.match?.takeUnless { it.isOver }
    private fun updateMatch(match: MatchUi, clearNotice: Boolean = false) {
        _state.update { it.copy(match = match, notice = if (clearNotice) null else it.notice) }
    }
    /**
     * Resolves a round with the engine, reports used power-ups, updates the log and replay events, then
     * either prepares the next round (choosing the bot's next action) or reports the result.
     */
    private fun resolve(match: MatchUi, plrAction: TurnAction, finishedTmr: TurnTmr) {
        val result = CombatEngine.resRound(
            match.combat, plrAction, checkNotNull(botAction), checkNotNull(combatRng),
        )
        if (BuildConfig.DEBUG) {
            Log.d("Arena", "round ${match.combat.round}: $plrAction vs $botAction, hp ${result.state.p1.hp}/${result.state.p2.hp}")
        }
        result.evts.filterIsInstance<CombatEvt.PwrUpUsed>()
            .filter { it.plr == PlrId.P1 }
            .forEach { matchSink.onPwrUpUsed(it.cardId) }
        val next = if (result.state.isOver) {
            botAction = null
            MatchUi(result.state, legalMoves = emptySet())
        } else {
            val rng = checkNotNull(botRng)
            val st = result.state
            botAction = BotBrain.chooseAction(st.p2, st.p1, _state.value.botTier, rng)
            botDelayMs = BotBrain.respDelayMs(rng)
            val plr = st.p1
            MatchUi(
                combat = st,
                legalMoves = CombatEngine.legalMoves(plr),
                switchableSlots = plr.sticks.filter { (slot, s) -> !s.brkn && slot != plr.activeSlot }.keys.sorted(),
                pwrUpSlots = plr.deck.indices.filter { plr.pwrUpAt(it) != null },
            )
        }
        _state.update {
            it.copy(
                match = next,
                log = it.log + ArenaLog.lines(result.evts),
                notice = null,
                lastRound = RoundPb((it.lastRound?.seq ?: 0) + 1, result.evts),
            )
        }
        _tmr.value = if (result.state.isOver) finishedTmr else finishedTmr.reset()
        if (result.state.isOver) {
            matchSink.onMatchEnded(MatchOutcome(matchId, won = result.state.winner == PlrId.P1))
        }
    }
    /** Picks the bot's action for the first round. */
    private fun beginRound(combat: CombatState): MatchUi {
        val rng = checkNotNull(botRng)
        botAction = BotBrain.chooseAction(combat.p2, combat.p1, _state.value.botTier, rng)
        botDelayMs = BotBrain.respDelayMs(rng)
        return MatchUi.forRound(combat)
    }
    companion object {
        /** Domain tag so the bot's deck RNG is independent of the combat RNG. */
        private val BOT_DECK_DOMAIN = "stick-deck-duel/bot-deck".encodeToByteArray()
        /** Generates the bot deck for [tier] from the match seed. */
        fun defBotDeck(tier: Int, seed: ByteArray): List<Card> =
            BotDeckGenerator.gen(tier, DeterministicRng.fromBytes(Hash.sha256(seed, BOT_DECK_DOMAIN)))
    }
}
