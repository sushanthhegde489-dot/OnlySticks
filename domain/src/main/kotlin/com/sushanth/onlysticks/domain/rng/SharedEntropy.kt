package com.sushanth.onlysticks.domain.rng

import java.security.SecureRandom

object SharedEntropy {
    const val NONCE_SIZE = 32

    fun newNonce(random: SecureRandom = SecureRandom()): ByteArray = ByteArray(NONCE_SIZE).also(random::nextBytes)

    fun shrdSeed(plr1Nonce: ByteArray, plr2Nonce: ByteArray): ByteArray {
        require(plr1Nonce.size == NONCE_SIZE && plr2Nonce.size == NONCE_SIZE) { "nonces must be $NONCE_SIZE bytes" }
        return Hash.sha256(plr1Nonce, plr2Nonce)
    }

    fun rngFor(plr1Nonce: ByteArray, plr2Nonce: ByteArray): DeterministicRng =
        DeterministicRng.fromBytes(shrdSeed(plr1Nonce, plr2Nonce))
}
