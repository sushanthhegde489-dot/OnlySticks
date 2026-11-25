// HMAC signing of card rows so edits made directly to the database can be detected.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.sushanth.onlysticks.domain.rng.Hash.hexToBytesOrNull
import com.sushanth.onlysticks.domain.rng.Hash.toHex
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

/** Signs and verifies card data with HMAC-SHA256. [key] is supplied lazily so the Keystore is only touched when needed. */
class CardSigner(private val key: () -> SecretKey) {
    /** Hex HMAC of [fields]. */
    fun sign(fields: ByteArray): String = mac().doFinal(fields).toHex()

    /** Constant-time check that [signature] matches [fields]. */
    fun verify(fields: ByteArray, signature: String): Boolean {
        val given = signature.hexToBytesOrNull() ?: return false
        return MessageDigest.isEqual(mac().doFinal(fields), given)
    }

    private fun mac(): Mac = Mac.getInstance(ALGORITHM).apply { init(key()) }

    companion object {
        const val ALIAS = "stickdeck.card.sign"
        private const val ALGORITHM = "HmacSHA256"

        /** Signer backed by a non-exportable HMAC key in Android Keystore. */
        fun fromKeystore(alias: String = ALIAS): CardSigner {
            val key by lazy { ksKey(alias) }
            return CardSigner { key }
        }

        /** Loads the key with this alias, generating it on first use. */
        private fun ksKey(alias: String): SecretKey {
            val ks = KeyStore.getInstance(KsKeyWrap.ANDROID_KEYSTORE).apply { load(null) }
            (ks.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
            val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, KsKeyWrap.ANDROID_KEYSTORE)
            gen.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY).build())
            return gen.generateKey()
        }
    }
}
