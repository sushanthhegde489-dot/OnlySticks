package com.sushanth.onlysticks.health

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class StepSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val stpSync: StpSync,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        when (val outcome = stpSync.run()) {
            StpSyncOutcome.NoPerm -> Log.i(TAG, "Step access not granted or Health Connect unavailable; skipped")
            is StpSyncOutcome.Rejected -> Log.i(TAG, "Steps for ${outcome.date} not claimed: ${outcome.reason}")
            is StpSyncOutcome.Claimed -> Log.i(TAG, "Claimed ${outcome.date}: +${outcome.coins} coins, +${outcome.affinity} affinity")
        }
        Result.success()
    } catch (e: SecurityException) {
        Log.w(TAG, "Step access denied", e)
        Result.success()
    } catch (e: IllegalStateException) {
        Log.w(TAG, "Health Connect unavailable", e)
        Result.success()
    }

    companion object {
        const val TAG = "StepSync"
    }
}

object StpSyncScheduler {
    const val UNIQUE_NAME = "step-sync-daily"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<StepSyncWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
