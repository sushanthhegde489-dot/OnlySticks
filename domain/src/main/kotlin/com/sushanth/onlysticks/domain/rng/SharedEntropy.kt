// Builds a shared match seed from two players' random nonces.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.rng

import java.security.SecureRandom

/** Each player contributes a nonce; hashing both means neither side alone can choose the seed. */
object SharedEntropy {
    const val NONCE_SIZE = 32

    /** Fresh 32-byte random nonce. */
    fun newNonce(random: SecureRandom = SecureRandom()): ByteArray = ByteArray(NONCE_SIZE).also(random::nextBytes)

    /** Shared seed = SHA-256(P1 nonce, P2 nonce). */
    fun shrdSeed(plr1Nonce: ByteArray, plr2Nonce: ByteArray): ByteArray {
        require(plr1Nonce.size == NONCE_SIZE && plr2Nonce.size == NONCE_SIZE) { "nonces must be $NONCE_SIZE bytes" }
        return Hash.sha256(plr1Nonce, plr2Nonce)
    }

    /** RNG seeded from both nonces. */
    fun rngFor(plr1Nonce: ByteArray, plr2Nonce: ByteArray): DeterministicRng =
        DeterministicRng.fromBytes(shrdSeed(plr1Nonce, plr2Nonce))
}
