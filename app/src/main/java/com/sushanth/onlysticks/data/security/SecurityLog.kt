// Logging hook for security events (e.g. a tampered card).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.security

import android.util.Log

/** Receives security warnings. [ANDROID] writes to Logcat; tests can supply their own. */
fun interface SecurityLog {
    fun warn(message: String)

    companion object {
        const val TAG = "OnlySticksSecurity"
        val ANDROID = SecurityLog { Log.w(TAG, it) }
    }
}
