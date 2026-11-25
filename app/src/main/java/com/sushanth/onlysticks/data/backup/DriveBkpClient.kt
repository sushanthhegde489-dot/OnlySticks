// Minimal Google Drive REST client (no Drive SDK) for the hidden app-data folder.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant

/** Metadata about a backup file on Drive (card count and coins are shown before restoring). */
data class DriveBkpFile(val id: String, val savedAt: Long, val cardCnt: Int?, val coins: Long?)

/** A non-2xx Drive response. Rate limits (429) and server errors (5xx) can be retried. */
class DriveHttpException(val code: Int) : IOException("Drive request failed: HTTP $code") {
    val retryable: Boolean get() = code == 429 || code >= 500
}

/** Uploads, lists and downloads backup files using plain HttpURLConnection. */
class DriveBkpClient {
    /** Multipart upload of a new backup, then deletes all but the newest [keep] backups. */
    suspend fun up(token: String, content: String, cardCnt: Int, coins: Long, keep: Int) = withContext(Dispatchers.IO) {
        val props = JSONObject().put("cards", cardCnt.toString()).put("coins", coins.toString())
        val meta = JSONObject()
            .put("name", "$NAME_PREFIX-${System.currentTimeMillis()}.json")
            .put("parents", JSONArray().put("appDataFolder"))
            .put("appProperties", props)
        val bound = "onlysticks-${System.nanoTime()}"
        val body = buildString {
            append("--$bound\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n").append(meta).append("\r\n")
            append("--$bound\r\nContent-Type: application/json\r\n\r\n").append(content).append("\r\n--$bound--")
        }
        request(token, "POST", "$UPLOAD?uploadType=multipart", body, "multipart/related; boundary=$bound")
        listFiles(token).drop(keep).forEach { runCatching { request(token, "DELETE", "$FILES/${it.id}") } }
    }

    /** All backups, newest first. */
    suspend fun list(token: String): List<DriveBkpFile> = withContext(Dispatchers.IO) { listFiles(token) }

    /** Downloads a backup's JSON content. */
    suspend fun down(token: String, id: String): String = withContext(Dispatchers.IO) {
        request(token, "GET", "$FILES/$id?alt=media")
    }

    /** Queries the appDataFolder for backup files and parses their metadata. */
    private fun listFiles(token: String): List<DriveBkpFile> {
        val q = URLEncoder.encode("name contains '$NAME_PREFIX' and trashed = false", "UTF-8")
        val fields = URLEncoder.encode("files(id,modifiedTime,appProperties)", "UTF-8")
        val body = request(token, "GET", "$FILES?spaces=appDataFolder&orderBy=modifiedTime%20desc&pageSize=50&fields=$fields&q=$q")
        val arr = JSONObject(body).getJSONArray("files")
        return (0 until arr.length()).map { i ->
            val f = arr.getJSONObject(i)
            val props = f.optJSONObject("appProperties")
            DriveBkpFile(
                id = f.getString("id"),
                savedAt = Instant.parse(f.getString("modifiedTime")).toEpochMilli(),
                cardCnt = props?.optString("cards")?.toIntOrNull(),
                coins = props?.optString("coins")?.toLongOrNull(),
            )
        }
    }

    /** Performs one authenticated HTTP request and returns the response body. Throws [DriveHttpException] on errors. */
    private fun request(token: String, method: String, url: String, body: String? = null, contentType: String? = null): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", contentType)
                conn.outputStream.use { it.write(body.encodeToByteArray()) }
            }
            val code = conn.responseCode
            if (code !in 200..299) throw DriveHttpException(code)
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private companion object {
        const val NAME_PREFIX = "onlysticks-backup"
        const val FILES = "https://www.googleapis.com/drive/v3/files"
        const val UPLOAD = "https://www.googleapis.com/upload/drive/v3/files"
        const val TIMEOUT_MS = 20_000
    }
}
