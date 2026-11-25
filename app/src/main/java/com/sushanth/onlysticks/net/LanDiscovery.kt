// Finds and advertises PvP games on the local Wi-Fi network using Android NSD (mDNS / DNS-SD).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.net

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.pvp.TcpClient
import com.sushanth.onlysticks.domain.pvp.TcpHost
import com.sushanth.onlysticks.ui.pvp.LanHost
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.Inet4Address
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/** Local-network PvP: hosting, joining by address, advertising a game and discovering nearby games. */
class LanPvpNetwork(context: Context) {
    private val nsd = context.applicationContext.getSystemService(NsdManager::class.java)
    private val resLock = Mutex()

    /** Starts a TCP server for an opponent to join. */
    fun host() = TcpHost()

    /** Connects to a host at [address]. */
    suspend fun join(address: String, port: Int = Cfg.Pvp.PORT) = TcpClient.connect(address.trim(), port)

    /** Advertises a hosted game named [name] with its stake value. Close the returned handle to stop advertising. */
    fun advertise(name: String, port: Int, betVal: Int): AutoCloseable {
        val info = NsdServiceInfo().apply {
            serviceName = SERVICE_PREFIX + name.take(MAX_NAME)
            serviceType = Cfg.Pvp.SERVICE_TYPE
            setPort(port)
            setAttribute(STAKE_KEY, betVal.toString())
        }
        val lstnr = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {}
            override fun onRegistrationFailed(info: NsdServiceInfo, code: Int) {}
            override fun onServiceUnregistered(info: NsdServiceInfo) {}
            override fun onUnregistrationFailed(info: NsdServiceInfo, code: Int) {}
        }
        runCatching { nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, lstnr) }
        return AutoCloseable { runCatching { nsd.unregisterService(lstnr) } }
    }

    /** Live list of OnlySticks games found on the network. Each service is resolved to an IPv4 address. */
    fun discover(): Flow<List<LanHost>> = callbackFlow {
        val found = linkedMapOf<String, LanHost>()
        val lstnr = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(type: String) {}
            override fun onDiscoveryStopped(type: String) {}
            override fun onStartDiscoveryFailed(type: String, code: Int) { close() }
            override fun onStopDiscoveryFailed(type: String, code: Int) {}
            override fun onServiceFound(info: NsdServiceInfo) {
                if (!info.serviceName.startsWith(SERVICE_PREFIX)) return
                launch {
                    val host = resolve(info) ?: return@launch
                    found[info.serviceName] = host
                    trySend(found.values.toList())
                }
            }
            override fun onServiceLost(info: NsdServiceInfo) {
                if (found.remove(info.serviceName) != null) trySend(found.values.toList())
            }
        }
        trySend(emptyList())
        runCatching { nsd.discoverServices(Cfg.Pvp.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, lstnr) }.onFailure { close() }
        awaitClose { runCatching { nsd.stopServiceDiscovery(lstnr) } }
    }

    /** Resolves one service at a time (older Android versions only allow one resolve in flight). */
    private suspend fun resolve(info: NsdServiceInfo): LanHost? = resLock.withLock {
        val res = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) resModern(info) else resLegacy(info)
        res?.let { r ->
            val address = ipv4Of(r) ?: return@let null
            val bet = r.attributes[STAKE_KEY]?.decodeToString()?.toIntOrNull()?.takeIf { it >= 0 }
            LanHost(r.serviceName.removePrefix(SERVICE_PREFIX), address, r.port, bet)
        }
    }

    /** First IPv4 address of a resolved service. */
    private fun ipv4Of(info: NsdServiceInfo): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            info.hostAddresses.firstOrNull { it is Inet4Address }?.hostAddress
        } else {
            @Suppress("DEPRECATION")
            info.host?.takeIf { it is Inet4Address }?.hostAddress
        }

    @Suppress("DEPRECATION")
    /** Resolve for Android 13 and below. */
    private suspend fun resLegacy(info: NsdServiceInfo): NsdServiceInfo? = suspendCancellableCoroutine { cont ->
        nsd.resolveService(info, object : NsdManager.ResolveListener {
            override fun onResolveFailed(info: NsdServiceInfo, code: Int) { if (cont.isActive) cont.resume(null) }
            override fun onServiceResolved(info: NsdServiceInfo) { if (cont.isActive) cont.resume(info) }
        })
    }

    /** Resolve for Android 14+ using ServiceInfoCallback. */
    private suspend fun resModern(info: NsdServiceInfo): NsdServiceInfo? = suspendCancellableCoroutine { cont ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { cont.resume(null); return@suspendCancellableCoroutine }
        val cb = object : NsdManager.ServiceInfoCallback {
            override fun onServiceInfoCallbackRegistrationFailed(code: Int) { if (cont.isActive) cont.resume(null) }
            override fun onServiceUpdated(updated: NsdServiceInfo) {
                if (updated.hostAddresses.isNotEmpty() && cont.isActive) {
                    cont.resume(updated)
                    runCatching { nsd.unregisterServiceInfoCallback(this) }
                }
            }
            override fun onServiceLost() {}
            override fun onServiceInfoCallbackUnregistered() {}
        }
        nsd.registerServiceInfoCallback(info, executor, cb)
        cont.invokeOnCancellation { runCatching { nsd.unregisterServiceInfoCallback(cb) } }
    }

    private companion object {
        /** Prefix added to service names so we only list our own games. */
        const val SERVICE_PREFIX = "OnlySticks-"
        const val MAX_NAME = 40
        const val STAKE_KEY = "stake"
        val executor = Executors.newSingleThreadExecutor()
    }
}
