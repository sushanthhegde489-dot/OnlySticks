package com.sushanth.onlysticks.account

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sushanth.onlysticks.data.backup.DriveHttpException
import kotlinx.coroutines.CancellationException

class BkpWorker(
    context: Context,
    params: WorkerParameters,
    private val cloud: CloudBkp,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        val res = cloud.syncNow()
        Log.i(TAG, "auto backup: $res")
        Result.success()
    } catch (e: CancellationException) {
        throw e
    } catch (e: DriveHttpException) {
        Log.w(TAG, "auto backup failed", e)
        if (e.retryable && runAttemptCount < MAX_TRIES) Result.retry() else Result.failure()
    } catch (e: Exception) {
        Log.w(TAG, "auto backup failed", e)
        if (runAttemptCount < MAX_TRIES) Result.retry() else Result.failure()
    }

    private companion object {
        const val TAG = "BkpWorker"
        const val MAX_TRIES = 5
    }
}
