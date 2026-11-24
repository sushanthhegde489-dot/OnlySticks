package com.sushanth.onlysticks.data.security

import android.util.Log

fun interface SecurityLog {
    fun warn(message: String)

    companion object {
        const val TAG = "OnlySticksSecurity"
        val ANDROID = SecurityLog { Log.w(TAG, it) }
    }
}
