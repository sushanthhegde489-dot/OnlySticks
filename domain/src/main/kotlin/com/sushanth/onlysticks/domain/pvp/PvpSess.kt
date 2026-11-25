// PvP session protocol on top of a [LineChan].
// Uses commit-reveal for every secret exchange (nonces, loadouts, turn actions) so neither player can
// wait to see the other's choice before making their own.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.pvp

import com.sushanth.onlysticks.domain.combat.PlrId
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TurnAction
import com.sushanth.onlysticks.domain.rng.Hash
import com.sushanth.onlysticks.domain.rng.Hash.toHex
import com.sushanth.onlysticks.domain.rng.SharedEntropy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.security.SecureRandom

/** One PvP connection. [seat] is P1 for the host and P2 for the guest. */
class PvpSess(
    private val chan: LineChan,
    val seat: PlrId,
    scope: CoroutineScope,
    private val timeoutMs: Long = Cfg.Pvp.ROUND_TIMEOUT_MS,
    private val random: SecureRandom = SecureRandom(),
) {
    /** Messages read in the background, waiting to be consumed by [expect]. */
    private val inbox = Channel<Result<List<String>>>(INBOX_SIZE)
    private val _peerCommittedRound = MutableStateFlow(0)
    /** Latest round the opponent has committed an action for (lets the UI show "opponent is ready"). */
    val peerCommittedRound: StateFlow<Int> = _peerCommittedRound.asStateFlow()

    /**
     * Background reader: splits incoming lines into words, tracks round commits, and closes the
     * session if the peer sends faster than we consume (flooding).
     */
    private val reader = scope.launch {
        while (true) {
            val line = runCatching { chan.receive() }
            val parts = line.map { it.split(" ") }
            parts.getOrNull()?.let { if (it[0] == COMMIT) roundOf(it.getOrNull(1))?.let { r -> _peerCommittedRound.value = r } }
            if (inbox.trySend(parts).isFailure) {
                chan.close()
                inbox.close(ProtocolException("peer is flooding"))
                break
            }
            if (line.isFailure) break
        }
    }

    /** Handshake: exchange protocol version and display names. Returns the opponent's name. */
    suspend fun shake(myName: String): String {
        chan.send("$HELLO $VERSION ${PvpCodec.encodeTxt(myName)}")
        val reply = expect(HELLO, 3)
        if (reply[1] != VERSION) throw ProtocolException("version mismatch")
        return PvpCodec.decodeTxt(reply[2])
    }

    /** Exchanges random nonces used to build the shared match seed. */
    suspend fun exchangeNonce(myNonce: ByteArray): ByteArray {
        val theirs = commitShow("nonce", myNonce)
        if (theirs.size != SharedEntropy.NONCE_SIZE) throw ProtocolException("bad nonce")
        return theirs
    }

    /** Exchanges deck, trainer and stake. Returns the opponent's (not yet validated) loadout. */
    suspend fun exchangeLoadout(mine: Loadout): Loadout {
        val text = listOf(PvpCodec.encodeCards(mine.deck), PvpCodec.encodeCards(listOfNotNull(mine.trainer)), PvpCodec.encodeCards(mine.bet))
            .joinToString("|")
        val parts = commitShow("loadout", text.encodeToByteArray()).decodeToString().split("|")
        if (parts.size != 3) throw ProtocolException("bad loadout")
        val trainer = PvpCodec.decodeCards(parts[1]).let { it.singleOrNull() ?: if (it.isEmpty()) null else throw ProtocolException("bad trainer") }
        return Loadout(
            deck = PvpCodec.decodeCards(parts[0]),
            trainer = trainer as? Trnr ?: trainer?.let { throw ProtocolException("bad trainer") },
            bet = PvpCodec.decodeCards(parts[2]).map { it as? Stick ?: throw ProtocolException("bad stake") },
        )
    }

    /** Exchanges this round's actions. */
    suspend fun exchangeAction(round: Int, action: TurnAction): TurnAction =
        PvpCodec.decodeAction(commitShow("$ROUND_PREFIX$round", PvpCodec.encodeAction(action).encodeToByteArray()).decodeToString())

    fun close() {
        reader.cancel()
        chan.close()
    }

    /** Commit-reveal: send a salted hash, wait for theirs, then reveal value+salt and verify their reveal. */
    private suspend fun commitShow(tag: String, value: ByteArray): ByteArray {
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        chan.send("$COMMIT $tag ${Hash.commit(value, salt).toHex()}")
        val commitment = expect(COMMIT, 3, tag)[2]
        chan.send("$REVEAL $tag ${PvpCodec.encodeBytes(value)} ${PvpCodec.encodeBytes(salt)}")
        val show = expect(REVEAL, 4, tag)
        val theirVal = PvpCodec.decodeBytes(show[2])
        val theirSalt = PvpCodec.decodeBytes(show[3])
        if (!Hash.verify(PvpCodec.hexToBytes(commitment), theirVal, theirSalt)) throw ProtocolException("reveal does not match commitment")
        return theirVal
    }

    /** Waits for the next message and checks its type, word count and tag. Throws on timeout, BYE or mismatch. */
    private suspend fun expect(kind: String, size: Int, tag: String? = null): List<String> {
        val parts = withTimeout(timeoutMs) { inbox.receive() }.getOrElse { throw it as? ProtocolException ?: ChanClosedException() }
        if (parts[0] == BYE) throw ChanClosedException()
        if (parts[0] != kind || parts.size != size || (tag != null && parts[1] != tag)) throw ProtocolException("unexpected ${parts[0]}")
        return parts
    }

    /** Parses the round number from tags like "r12". */
    private fun roundOf(tag: String?): Int? = tag?.removePrefix(ROUND_PREFIX)?.takeIf { tag.startsWith(ROUND_PREFIX) }?.toIntOrNull()

    private companion object {
        const val VERSION = "1"
        const val HELLO = "HELLO"
        const val COMMIT = "COMMIT"
        const val REVEAL = "REVEAL"
        const val BYE = "BYE"
        const val ROUND_PREFIX = "r"
        const val SALT_SIZE = 32
        const val INBOX_SIZE = 16
    }
}
