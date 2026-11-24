package com.sushanth.onlysticks.domain.pvp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

interface LineChan {
    suspend fun send(line: String)
    suspend fun receive(): String
    fun close()
}

class ChanClosedException : Exception("connection closed")

class SocketLineChan(private val socket: Socket) : LineChan {
    private val input = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
    private val output = PrintWriter(socket.getOutputStream().bufferedWriter(Charsets.UTF_8), true)

    override suspend fun send(line: String) = withContext(Dispatchers.IO) {
        output.println(line)
        if (output.checkError()) throw ChanClosedException()
    }

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
        const val MAX_LINE = 16 * 1024
    }
}
