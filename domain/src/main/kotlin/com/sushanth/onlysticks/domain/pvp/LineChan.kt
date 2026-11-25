// Line-based text channel used by local PvP. Each protocol message is one line of UTF-8 text.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.pvp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

/** A bidirectional channel that sends and receives whole lines. */
interface LineChan {
    suspend fun send(line: String)
    suspend fun receive(): String
    fun close()
}

/** Thrown when the other side disconnects. */
class ChanClosedException : Exception("connection closed")

/** [LineChan] over a TCP socket. Lines are capped at MAX_LINE characters to protect against abuse. */
class SocketLineChan(private val socket: Socket) : LineChan {
    private val input = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
    private val output = PrintWriter(socket.getOutputStream().bufferedWriter(Charsets.UTF_8), true)

    override suspend fun send(line: String) = withContext(Dispatchers.IO) {
        output.println(line)
        if (output.checkError()) throw ChanClosedException()
    }

    /** Reads characters until a newline. Ignores carriage returns. Throws if the line grows too long. */
    override suspend fun receive(): String = withContext(Dispatchers.IO) {
        val line = StringBuilder()
        while (true) {
            val c = input.read()
            if (c < 0) throw ChanClosedException()
            if (c == '\n'.code) break
            if (c == '\r'.code) continue
            if (line.length >= MAX_LINE) {
                close()
                throw ProtocolException("line too long")
            }
            line.append(c.toChar())
        }
        line.toString()
    }

    override fun close() {
        runCatching { socket.close() }
    }

    companion object {
        /** Longest accepted line (16 KiB). */
        const val MAX_LINE = 16 * 1024
    }
}
