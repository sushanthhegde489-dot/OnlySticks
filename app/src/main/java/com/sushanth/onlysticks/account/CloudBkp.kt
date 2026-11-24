package com.sushanth.onlysticks.account

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sushanth.onlysticks.data.backup.BkpFormatException
import com.sushanth.onlysticks.data.backup.BkpJson
import com.sushanth.onlysticks.data.backup.BackupRepository
import com.sushanth.onlysticks.data.backup.DriveBkpClient
import com.sushanth.onlysticks.data.backup.DriveBkpFile
import com.sushanth.onlysticks.data.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.util.concurrent.TimeUnit

enum class SyncRes { DONE, SKIPPED, NO_ACCESS }

class CloudBkp(
    private val goog: GoogAcctClient,
    private val drive: DriveBkpClient,
    private val bkp: BackupRepository,
    private val prof: ProfileRepository,
    private val clock: Clock,
) {
    private val mutex = Mutex()

    suspend fun up(token: String) = mutex.withLock {
        val snap = bkp.export()
        drive.up(token, BkpJson.encode(snap), snap.cardList.size, snap.ldgr.sumOf { it.amount }, KEEP_COPIES)
        prof.markBackedUp(clock.millis())
    }

    suspend fun latest(token: String): DriveBkpFile? = drive.list(token).firstOrNull()

    suspend fun restore(token: String): Boolean = mutex.withLock {
        for (f in drive.list(token)) {
            val snap = try {
                BkpJson.decode(drive.down(token, f.id))
            } catch (e: BkpFormatException) {
                Log.w(TAG, "backup ${f.id} is damaged, trying an older one", e)
                continue
            }
            bkp.restore(snap)
            prof.markBackedUp(clock.millis())
            return@withLock true
        }
        false
    }

    suspend fun syncNow(force: Boolean = false): SyncRes {
        val acct = prof.observeAcct().first()
        val email = acct.googleEmail ?: return SyncRes.SKIPPED
        if (acct.lastBackupAt == null && !force) return SyncRes.SKIPPED
        return when (val access = goog.driveAccess(email)) {
            is DriveAccess.Granted -> {
                up(access.token)
                SyncRes.DONE
            }
            is DriveAccess.NeedsConsent -> SyncRes.NO_ACCESS
        }
    }

    @OptIn(FlowPreview::class)
    fun autoBkp(scope: CoroutineScope, changes: Flow<Any>, work: WorkManager) = scope.launch {
        changes.drop(1).debounce(AUTO_BACKUP_DELAY_MS).collect { queue(work) }
    }

    private fun queue(work: WorkManager) {
        val req = OneTimeWorkRequestBuilder<BkpWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInitialDelay(QUEUE_DELAY_SECS, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, RETRY_SECS, TimeUnit.SECONDS)
            .build()
        work.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, req)
    }

    private companion object {
        const val TAG = "CloudBackup"
        const val WORK_NAME = "drive-backup"
        const val AUTO_BACKUP_DELAY_MS = 2_000L
        const val QUEUE_DELAY_SECS = 10L
        const val RETRY_SECS = 30L
        const val KEEP_COPIES = 3
    }
}
