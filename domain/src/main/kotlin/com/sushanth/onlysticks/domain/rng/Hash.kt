package com.sushanth.onlysticks.domain.rng

import java.nio.ByteBuffer
import java.security.MessageDigest

object Hash {
    private val COMMIT_TAG = "stickdeck/commit/v1".toByteArray()

    fun sha256(vararg parts: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        parts.forEach { md.update(it) }
        return md.digest()
    }

    fun commit(value: ByteArray, salt: ByteArray): ByteArray =
        sha256(COMMIT_TAG, lenOf(value), value, lenOf(salt), salt)
    fun verify(commitment: ByteArray, value: ByteArray, salt: ByteArray): Boolean =
        MessageDigest.isEqual(commitment, commit(value, salt))

    private fun lenOf(bytes: ByteArray): ByteArray = ByteBuffer.allocate(4).putInt(bytes.size).array()
    fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

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
