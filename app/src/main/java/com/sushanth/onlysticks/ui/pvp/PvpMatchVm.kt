// ViewModel for local-network PvP: hosting, joining, discovery and running the match in lockstep.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.pvp

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.combat.CombatEngine
import com.sushanth.onlysticks.domain.combat.CombatEvt
import com.sushanth.onlysticks.domain.combat.CombatState
import com.sushanth.onlysticks.domain.combat.Perspective
import com.sushanth.onlysticks.domain.combat.PlrId
import com.sushanth.onlysticks.domain.combat.TurnTmr
import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Deck
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpTarget
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.TurnAction
import com.sushanth.onlysticks.domain.pvp.LineChan
import com.sushanth.onlysticks.domain.pvp.Loadout
import com.sushanth.onlysticks.domain.pvp.LoadoutProblem
import com.sushanth.onlysticks.domain.pvp.LoadoutRules
import com.sushanth.onlysticks.domain.pvp.ProtocolException
import com.sushanth.onlysticks.domain.pvp.PvpSess
import com.sushanth.onlysticks.domain.pvp.TcpHost
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import com.sushanth.onlysticks.domain.rng.SharedEntropy
import com.sushanth.onlysticks.domain.trainer.TrainerProgression
import com.sushanth.onlysticks.net.LanPvpNetwork
import com.sushanth.onlysticks.ui.arena.ArenaLog
import com.sushanth.onlysticks.ui.arena.ArenaUiState
import com.sushanth.onlysticks.ui.arena.DuelCtrl
import com.sushanth.onlysticks.ui.arena.MatchUi
import com.sushanth.onlysticks.ui.arena.OppLbl
import com.sushanth.onlysticks.ui.arena.RoundPb
import com.sushanth.onlysticks.ui.common.uiTxt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Runs a PvP match over the LAN. Both devices simulate the same deterministic combat from a shared seed,
 * exchanging only their turn actions (via commit-reveal), so neither side has to trust the other's results.
 * The host is always P1 in the canonical state; the guest sees a flipped view via [Perspective].
 */
class PvpMatchVm(
    private val network: LanPvpNetwork,
    private val localAddr: () -> String? = { null },
    private val sink: PvpMatchSink = PvpMatchSink.NONE,
    private val turnDurMs: Long = Cfg.TURN_TIMER_MS,
) : ViewModel(), DuelCtrl {
    /** Connection progress shown by the PvP lobby. */
    private val _phase = MutableStateFlow<PvpPhase>(PvpPhase.Idle)
    /** Games found on the network while browsing. */
    private val _nearby = MutableStateFlow<List<LanHost>>(emptyList())
    val nearbyHosts: StateFlow<List<LanHost>> = _nearby.asStateFlow()
    private var browsing: Job? = null
    private var advert: AutoCloseable? = null
    val phase: StateFlow<PvpPhase> = _phase.asStateFlow()

    private val _state = MutableStateFlow(ArenaUiState(canRematch = false))
    override val state: StateFlow<ArenaUiState> = _state.asStateFlow()
    private val _tmr = MutableStateFlow(TurnTmr(turnDurMs))
    override val tmr: StateFlow<TurnTmr> = _tmr.asStateFlow()

    private var job: Job? = null
    private var sess: PvpSess? = null
    private var server: TcpHost? = null
    /** The match state in host (P1) perspective, which both devices compute identically. */
    private var canonical: CombatState? = null
    /** Shared-seed RNG for combat. */
    private var rng: DeterministicRng? = null
    /** This device's seat: P1 when hosting, P2 when joining. */
    private var seat = PlrId.P1
    private var mine: Loadout? = null
    private var theirs: Loadout? = null

    /** Opens a server, advertises it on the network, waits for one opponent and then plays. */
    fun host(name: String, deck: Deck, bet: List<Stick>) = launchConn {
        val listening = runCatching { network.host() }.getOrElse { fail(PvpFailure.CANNOT_HOST); return@launchConn }
        server = listening
        stopBrowsing()
        advert = network.advertise(name, listening.port, Appr.tot(bet))
        _phase.value = PvpPhase.Hosting(localAddr(), listening.port)
        val chan = try { listening.awaitPlr() } finally { listening.close(); server = null; stopAdvertising() }
        play(chan, PlrId.P1, name, deck, bet)
    }

    /** Connects to a host at [address] and plays. */
    fun join(address: String, name: String, deck: Deck, bet: List<Stick>, port: Int = Cfg.Pvp.PORT) = launchConn {
        _phase.value = PvpPhase.Joining
        stopBrowsing()
        val chan = runCatching { network.join(address, port) }.getOrElse { fail(PvpFailure.CANNOT_CONNECT); return@launchConn }
        play(chan, PlrId.P2, name, deck, bet)
    }

    /** Starts listing nearby games. */
    fun startBrowsing() {
        if (browsing?.isActive == true) return
        browsing = viewModelScope.launch {
            runCatching { network.discover().collect { _nearby.value = it } }
        }
    }

    /** Stops listing nearby games. */
    fun stopBrowsing() {
        browsing?.cancel()
        browsing = null
        _nearby.value = emptyList()
    }

    private fun stopAdvertising() {
        advert?.close()
        advert = null
    }

    /** Abandons any connection or match and returns to the idle lobby. */
    fun cancel() {
        job?.cancel()
        closeSess()
        _phase.value = PvpPhase.Idle
        _state.value = ArenaUiState(canRematch = false)
    }

    /** Leaves a finished match (does nothing while it's still running). */
    override fun leaveMatch() {
        if (_state.value.match?.isOver == false) return
        cancel()
    }

    override fun selectMove(move: Move): Boolean = editRound(R.string.arena_notice_illegal_move) { if (move in it.legalMoves) it.withMove(move) else null }

    override fun selectSwtch(slot: Int?): Boolean =
        editRound(R.string.arena_notice_illegal_swtch) { if (slot == null || slot in it.switchableSlots) it.copy(selSwtchSlot = slot) else null }

    override fun selectPwrUp(slot: Int?): Boolean =
        editRound(R.string.arena_notice_illegal_powerup) { if (slot == null || slot in it.pwrUpSlots) it.withPwrUp(slot) else null }

    override fun selectPwrUpTarget(target: PwrUpTarget): Boolean =
        editRound(R.string.arena_notice_illegal_powerup) { if (it.selPwrUpSlot != null) it.copy(selPwrUpTarget = target) else null }

    /** Commits the selected action and waits for the opponent's. */
    override fun sbmt(): Boolean {
        val match = openRound() ?: return false
        val action = match.action() ?: return false
        _tmr.value = _tmr.value.sbmt()
        send(match, action)
        return true
    }

    /** Advances the turn timer, auto-submitting a skip when time runs out. Shows "ready" once the peer has committed. */
    override fun onTmrTick(elapsedMs: Long) {
        val match = openRound() ?: return
        val (next, evt) = _tmr.value.tick(elapsedMs)
        _tmr.value = next
        if (evt == TurnTmr.Event.AutoSkip) {
            send(match, TurnAction.AUTO_SKIP)
        } else {
            val ready = (sess?.peerCommittedRound?.value ?: 0) >= match.combat.round
            if (ready != match.oppReady) setMatch(match.copy(oppReady = ready))
        }
    }

    override fun pauseTmr() { if (openRound() != null) _tmr.value = _tmr.value.pause() }

    override fun resumeTmr() { if (openRound() != null) _tmr.value = _tmr.value.resume() }

    /** Closes the connection when the ViewModel is destroyed. */
    override fun onCleared() {
        closeSess()
    }

    /** Runs a connection job (one at a time) and turns errors into a failed phase. */
    private fun launchConn(block: suspend () -> Unit) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ProtocolException) {
                fail(PvpFailure.PROTOCOL)
            } catch (e: Exception) {
                fail(PvpFailure.DISCONNECTED)
            }
        }
    }

    /** Full match setup: handshake, nonce exchange, loadout exchange and validation, escrow, then the first round. */
    private suspend fun play(chan: LineChan, seat: PlrId, name: String, deck: Deck, bet: List<Stick>) {
        this.seat = seat
        _phase.value = PvpPhase.Handshaking
        val s = PvpSess(chan, seat, viewModelScope).also { sess = it }
        val oppName = s.shake(name)
        val myNonce = SharedEntropy.newNonce()
        val theirNonce = s.exchangeNonce(myNonce)
        val myLoadout = Loadout(deck.cardList, deck.trainer, bet)
        LoadoutRules.check(myLoadout)?.let { fail(PvpFailure.MY_LOADOUT_INVALID); return }
        val theirLoadout = s.exchangeLoadout(myLoadout)
        when (LoadoutRules.checkPair(myLoadout, theirLoadout)) {
            null -> Unit
            LoadoutProblem.BetsNotMatched -> { fail(PvpFailure.STAKES_NOT_MATCHED); return }
            else -> { fail(PvpFailure.OPPONENT_DECK_INVALID); return }
        }
        mine = myLoadout
        theirs = theirLoadout
        sink.onStarted(bet)
        val (host, guest) = if (seat == PlrId.P1) myLoadout to theirLoadout else theirLoadout to myLoadout
        rng = DeterministicRng.fromBytes(if (seat == PlrId.P1) SharedEntropy.shrdSeed(myNonce, theirNonce) else SharedEntropy.shrdSeed(theirNonce, myNonce))
        val start = CombatEngine.start(host.deck, guest.deck, TrainerProgression.modifiersFor(host.trainer), TrainerProgression.modifiersFor(guest.trainer))
        canonical = start
        _tmr.value = TurnTmr(turnDurMs)
        _state.value = ArenaUiState(match = MatchUi.forRound(Perspective.state(start, seat)), opp = OppLbl.Plr(oppName), canRematch = false)
        _phase.value = PvpPhase.InMatch
    }

    /** Exchanges this round's action with the opponent in the background, then resolves the round. */
    private fun send(match: MatchUi, action: TurnAction) {
        val s = sess ?: return
        val round = match.combat.round
        setMatch(match.copy(waitForOpp = true))
        viewModelScope.launch {
            try {
                val other = s.exchangeAction(round, action)
                resolve(action, other)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ProtocolException) {
                fail(PvpFailure.PROTOCOL)
            } catch (e: Exception) {
                fail(PvpFailure.DISCONNECTED)
            }
        }
    }

    /** Resolves a round with both actions (ordered as P1, P2) and updates this device's view of it. */
    private fun resolve(myAction: TurnAction, theirAction: TurnAction) {
        val before = canonical ?: return
        val (a1, a2) = if (seat == PlrId.P1) myAction to theirAction else theirAction to myAction
        val result = CombatEngine.resRound(before, a1, a2, checkNotNull(rng))
        canonical = result.state
        val evts = Perspective.evts(result.evts, seat)
        evts.filterIsInstance<CombatEvt.PwrUpUsed>().filter { it.plr == PlrId.P1 }.forEach { sink.onPwrUpUsed(it.cardId) }
        val v2 = Perspective.state(result.state, seat)
        val next = if (v2.isOver) MatchUi(v2, legalMoves = emptySet()) else MatchUi.forRound(v2)
        _state.update {
            it.copy(match = next, log = it.log + ArenaLog.lines(evts), notice = null, lastRound = RoundPb((it.lastRound?.seq ?: 0) + 1, evts))
        }
        _tmr.value = TurnTmr(turnDurMs).let { if (v2.isOver) it.sbmt() else it }
        if (v2.isOver) {
            sink.onSettled(v2.winner == PlrId.P1, mine?.bet.orEmpty(), theirs?.bet.orEmpty())
            closeSess()
        }
    }

    /** Ends the session with an error. A match that had started is voided so staked cards are released. */
    private fun fail(reason: PvpFailure) {
        if (_state.value.match?.isOver == true) return
        if (_state.value.match != null) sink.onVoided()
        closeSess()
        _state.value = ArenaUiState(canRematch = false)
        _phase.value = PvpPhase.Failed(reason)
    }

    /** Stops advertising and closes the server and session. */
    private fun closeSess() {
        stopAdvertising()
        server?.close()
        server = null
        sess?.close()
        sess = null
    }

    /** The current round if the player can still change their selection. */
    private fun openRound(): MatchUi? = _state.value.match?.takeUnless { it.isOver || it.waitForOpp }

    /** Applies [change] to the current selection, or shows [illegalNotice] if it returns null. */
    private fun editRound(@StringRes illegalNotice: Int, change: (MatchUi) -> MatchUi?): Boolean {
        val match = openRound() ?: return false
        val next = change(match)
        if (next == null) {
            _state.update { it.copy(notice = uiTxt(illegalNotice)) }
            return false
        }
        _state.update { it.copy(match = next, notice = null) }
        return true
    }

    private fun setMatch(match: MatchUi) = _state.update { it.copy(match = match) }
}
