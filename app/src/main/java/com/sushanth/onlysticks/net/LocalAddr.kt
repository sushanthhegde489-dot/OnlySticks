// Looks up this device's local IP address.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.net

import android.content.Context
import android.net.ConnectivityManager
import java.net.Inet4Address

/** Network helpers. */
object LocalAddr {
    /** This device's IPv4 address on the active network (shown so a friend can join manually), or null. */
    fun ipv4(context: Context): String? {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val props = cm.getLinkProperties(cm.activeNetwork) ?: return null
        return props.linkAddresses.map { it.address }.firstOrNull { it is Inet4Address && !it.isLoopbackAddress }?.hostAddress
    }
}
