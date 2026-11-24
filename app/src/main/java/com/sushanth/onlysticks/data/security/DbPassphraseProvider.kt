package com.sushanth.onlysticks.data.security

import java.io.File
import java.security.SecureRandom

class DbPassphraseProvider(
    private val fileDir: File,
    private val keyWrap: KeyWrap,
    private val random: SecureRandom = SecureRandom(),
) {
    val keyFile: File get() = File(fileDir, FILE_NAME)
    @Synchronized
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
