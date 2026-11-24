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

class CardSigner(private val key: () -> SecretKey) {
    fun sign(fields: ByteArray): String = mac().doFinal(fields).toHex()

    fun verify(fields: ByteArray, signature: String): Boolean {
        val given = signature.hexToBytesOrNull() ?: return false
        return MessageDigest.isEqual(mac().doFinal(fields), given)
    }

    private fun mac(): Mac = Mac.getInstance(ALGORITHM).apply { init(key()) }

    companion object {
        const val ALIAS = "stickdeck.card.sign"
        private const val ALGORITHM = "HmacSHA256"

        fun fromKeystore(alias: String = ALIAS): CardSigner {
            val key by lazy { ksKey(alias) }
            return CardSigner { key }
        }

        private fun ksKey(alias: String): SecretKey {
            val ks = KeyStore.getInstance(KsKeyWrap.ANDROID_KEYSTORE).apply { load(null) }
            (ks.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
            val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, KsKeyWrap.ANDROID_KEYSTORE)
            gen.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY).build())
            return gen.generateKey()
        }
    }
}
