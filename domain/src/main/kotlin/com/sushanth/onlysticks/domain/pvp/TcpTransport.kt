// TCP host/client used for local network PvP.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.pvp

import com.sushanth.onlysticks.domain.config.Cfg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/** Listens for one incoming opponent on [port]. */
class TcpHost(port: Int = Cfg.Pvp.PORT) {
    private val server = ServerSocket().apply {
        reuseAddress = true
        bind(InetSocketAddress(port))
    }

    /** Actual bound port (useful when port 0 was requested). */
    val port: Int get() = server.localPort

    /** Blocks (cancellably) until a player connects. */
    suspend fun awaitPlr(): LineChan = runInterruptible(Dispatchers.IO) {
        val socket = server.accept()
        socket.tcpNoDelay = true
        SocketLineChan(socket)
    }

    fun close() {
        runCatching { server.close() }
    }
}

/** Connects to a host on the local network. */
object TcpClient {
    suspend fun connect(host: String, port: Int = Cfg.Pvp.PORT): LineChan = runInterruptible(Dispatchers.IO) {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), Cfg.Pvp.CONNECT_TIMEOUT_MS)
        socket.tcpNoDelay = true
        SocketLineChan(socket)
    }
}
