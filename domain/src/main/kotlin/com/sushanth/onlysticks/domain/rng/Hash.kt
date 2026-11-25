// Hashing helpers: SHA-256, commit-reveal commitments and hex conversion.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.rng

import java.nio.ByteBuffer
import java.security.MessageDigest

/** Cryptographic helpers used by PvP commit-reveal and seed derivation. */
object Hash {
    /** Domain-separation prefix for commitments. */
    private val COMMIT_TAG = "stickdeck/commit/v1".toByteArray()

    /** SHA-256 of all [parts] concatenated. */
    fun sha256(vararg parts: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        parts.forEach { md.update(it) }
        return md.digest()
    }

    /** Commitment = SHA-256(tag, len(value), value, len(salt), salt). Length prefixes avoid ambiguity. */
    fun commit(value: ByteArray, salt: ByteArray): ByteArray =
        sha256(COMMIT_TAG, lenOf(value), value, lenOf(salt), salt)
    /** Constant-time check that ([value], [salt]) matches [commitment]. */
    fun verify(commitment: ByteArray, value: ByteArray, salt: ByteArray): Boolean =
        MessageDigest.isEqual(commitment, commit(value, salt))

    /** 4-byte big-endian length prefix. */
    private fun lenOf(bytes: ByteArray): ByteArray = ByteBuffer.allocate(4).putInt(bytes.size).array()
    /** Lower-case hex string. */
    fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    /** Parses a hex string, or returns null if it has odd length or non-hex characters. */
    fun String.hexToBytesOrNull(): ByteArray? {
        if (length % 2 != 0) return null
        return ByteArray(length / 2) { i ->
            val hi = Character.digit(this[2 * i], 16)
            val lo = Character.digit(this[2 * i + 1], 16)
            if (hi < 0 || lo < 0) return null
            ((hi shl 4) or lo).toByte()
        }
    }
}
