// Supplies the SQLCipher database passphrase.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.security

import java.io.File
import java.security.SecureRandom

/**
 * Generates a random 32-byte passphrase once, stores it encrypted by [keyWrap] in no-backup storage, and
 * decrypts it on later launches.
 */
class DbPassphraseProvider(
    private val fileDir: File,
    private val keyWrap: KeyWrap,
    private val random: SecureRandom = SecureRandom(),
) {
    val keyFile: File get() = File(fileDir, FILE_NAME)
    @Synchronized
    /**
     * Returns the passphrase, creating and saving it on first call. Written via a temp file + rename so a
     * crash can't leave a half-written key file.
     */
    fun passphrase(): ByteArray {
        val file = keyFile
        if (file.exists()) return keyWrap.unwrap(file.readBytes())
        val plain = ByteArray(PASSPHRASE_BYTES).also(random::nextBytes)
        fileDir.mkdirs()
        val tmp = File(fileDir, "$FILE_NAME.tmp")
        tmp.writeBytes(keyWrap.wrap(plain))
        check(tmp.renameTo(file)) { "Could not store the database key" }
        return plain
    }
    companion object {
        const val FILE_NAME = "db.key.enc"
        const val PASSPHRASE_BYTES = 32
    }
}
