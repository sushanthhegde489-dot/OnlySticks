// App-wide dependency container: creates the database, repositories and services once and hands them out.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.sushanth.onlysticks.account.BkpWorker
import com.sushanth.onlysticks.account.PlayStats
import com.sushanth.onlysticks.account.CloudBkp
import com.sushanth.onlysticks.account.GoogAcctClient
import com.sushanth.onlysticks.data.backup.BackupRepository
import com.sushanth.onlysticks.data.backup.DriveBkpClient
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.DbFactory
import com.sushanth.onlysticks.data.repository.AdminRepository
import com.sushanth.onlysticks.data.repository.CardRepository
import com.sushanth.onlysticks.data.repository.CoinsRepository
import com.sushanth.onlysticks.data.repository.DeckRepository
import com.sushanth.onlysticks.data.repository.MatchLifecycleSink
import com.sushanth.onlysticks.data.repository.MatchOutcome
import com.sushanth.onlysticks.data.repository.MatchRewards
import com.sushanth.onlysticks.data.repository.ProfileRepository
import com.sushanth.onlysticks.data.repository.PvpRewards
import com.sushanth.onlysticks.data.repository.RoomDeckBldrSrc
import com.sushanth.onlysticks.data.repository.SettingsRepository
import com.sushanth.onlysticks.data.repository.Stat
import com.sushanth.onlysticks.data.repository.StatsRepository
import com.sushanth.onlysticks.data.repository.ShopRepository
import com.sushanth.onlysticks.data.repository.StartSeed
import com.sushanth.onlysticks.data.repository.TrainerRepository
import com.sushanth.onlysticks.data.security.CardSigner
import com.sushanth.onlysticks.data.security.DbPassphraseProvider
import com.sushanth.onlysticks.data.security.KsKeyWrap
import com.sushanth.onlysticks.data.security.SecurityLog
import com.sushanth.onlysticks.domain.admin.AdminPolicy
import com.sushanth.onlysticks.domain.gacha.PackOpnr
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.store.WklyCardStore
import com.sushanth.onlysticks.health.HpConnectStpSrc
import com.sushanth.onlysticks.health.StpSrc
import com.sushanth.onlysticks.health.StpSync
import com.sushanth.onlysticks.health.StepSyncWorker
import com.sushanth.onlysticks.net.LanPvpNetwork
import com.sushanth.onlysticks.ui.deck.DeckBldrSrc
import com.sushanth.onlysticks.ui.pvp.PvpMatchSink
import java.time.Clock
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * Manual dependency-injection container (one per app process, owned by [OnlySticksApplication]).
 * Most members are lazy so nothing heavy is created until it's first needed.
 */
class AppCont(context: Context) {
    private val appContext = context.applicationContext

    val clock: Clock = Clock.systemDefaultZone()

    /** Logs crashes in background jobs instead of killing the app. */
    private val errors = CoroutineExceptionHandler { _, e -> Log.e(TAG, "Background task failed", e) }
    /** Long-lived scope for background work (database writes, backups) that outlives any screen. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errors)

    /** Signs card rows with a key from Android Keystore so tampered rows can be detected. */
    val signr: CardSigner by lazy { CardSigner.fromKeystore() }

    /** Encrypted (SQLCipher) Room database. The passphrase is generated once and wrapped by a Keystore key. */
    val db: AppDatabase by lazy {
        val passphrase = DbPassphraseProvider(appContext.noBackupFilesDir, KsKeyWrap()).passphrase()
        DbFactory.create(appContext, passphrase)
    }

    val cardRepo by lazy { CardRepository(db, signr, SecurityLog.ANDROID, clock) }
    val trainerRepo by lazy { TrainerRepository(db, clock) }
    val deckRepo by lazy { DeckRepository(db, cardRepo, trainerRepo) }
    val coinsRepo by lazy { CoinsRepository(db, clock) }
    val startSeed by lazy { StartSeed(db, cardRepo, deckRepo, coinsRepo, clock) }
    val matchRwds by lazy { MatchRewards(db, cardRepo, trainerRepo, coinsRepo, clock) }

    val settRepo by lazy { SettingsRepository(db) }
    val statsRepo by lazy { StatsRepository(settRepo) }
    val playStats by lazy { PlayStats(appContext, statsRepo) }
    val profileRepo by lazy { ProfileRepository(db, AdminPolicy.parse(BuildConfig.ADMIN_EMAILS)) }
    val adminRepo by lazy {
        AdminRepository(db, profileRepo, cardRepo, trainerRepo, coinsRepo, PackOpnr(), settRepo, clock)
    }
    val pvpNetwork by lazy { LanPvpNetwork(appContext) }
    /** Offline "debug" sign-in is only allowed in debug builds. */
    val offlineSignInAllowed: Boolean get() = BuildConfig.DEBUG
    val shopRepo by lazy {
        ShopRepository(db, cardRepo, trainerRepo, coinsRepo, PackOpnr(), WklyCardStore(), settRepo, clock, statsRepo)
    }
    private val pvpRwds by lazy { PvpRewards(db, cardRepo) }

    val googAcct by lazy { GoogAcctClient(appContext, BuildConfig.GOOGLE_WEB_CLIENT_ID) }
    val cloudBkp by lazy {
        CloudBkp(googAcct, DriveBkpClient(), BackupRepository(db, cardRepo, profileRepo, statsRepo, clock), profileRepo, clock)
    }

    /** Receives PvP match lifecycle callbacks and turns them into escrow/reward database writes. */
    val pvpSink: PvpMatchSink = object : PvpMatchSink {
        override fun onStarted(myBet: List<Stick>) {
            matchQueue.trySend { pvpRwds.hold(myBet) }
        }
        override fun onVoided() {
            matchQueue.trySend { pvpRwds.release() }
        }
        override fun onPwrUpUsed(cardId: String) {
            matchQueue.trySend { pvpRwds.pwrUpUsed(cardId) }
        }
        override fun onSettled(won: Boolean, myBet: List<Stick>, theirBet: List<Stick>) {
            matchQueue.trySend {
                pvpRwds.settle(won, myBet, theirBet)
                if (won) statsRepo.add(Stat.PVP_PLAYED, Stat.PVP_WON) else statsRepo.add(Stat.PVP_PLAYED)
            }
        }
    }

    val deckSrc: DeckBldrSrc by lazy {
        RoomDeckBldrSrc(cardRepo, trainerRepo, deckRepo, startSeed)
    }

    /** Guards so start-up work runs only once per process, even if the activity is recreated. */
    private val settledUnfinished = AtomicBoolean(false)
    private val autoBkpStarted = AtomicBoolean(false)

    /** Serial queue for match-related writes. Running them one at a time keeps escrow and rewards consistent. */
    private val matchQueue = Channel<suspend () -> Unit>(Channel.UNLIMITED).also { queue ->
        appScope.launch { for (job in queue) runCatching { job() }.onFailure { Log.e(TAG, "match write failed", it) } }
    }

    /** Receives PvE (bot) match lifecycle callbacks and records rewards and stats. */
    val matchSink: MatchLifecycleSink = object : MatchLifecycleSink {
        override fun onMatchStarted(matchId: String, botTier: Int, trainerId: String?) {
            matchQueue.trySend { matchRwds.start(matchId, botTier, trainerId) }
        }
        override fun onPwrUpUsed(cardId: String) {
            matchQueue.trySend { matchRwds.pwrUpUsed(cardId) }
        }
        override fun onMatchEnded(outcome: MatchOutcome) {
            matchQueue.trySend {
                matchRwds.end(outcome)
                statsRepo.add(Stat.PVE_PLAYED, if (outcome.won) Stat.PVE_WON else Stat.PVE_LOST)
            }
        }
    }

    val stpSrc: StpSrc by lazy { HpConnectStpSrc(appContext) }
    val stpSync by lazy { StpSync(db, stpSrc, trainerRepo, coinsRepo, clock) }
    /** Lets WorkManager construct our workers with their dependencies. */
    val workerFactory: WorkerFactory = object : WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
            when (workerClassName) {
                StepSyncWorker::class.java.name -> StepSyncWorker(appContext, workerParameters, stpSync)
                BkpWorker::class.java.name -> BkpWorker(appContext, workerParameters, cloudBkp)
                else -> null
            }
    }

    /**
     * Called from MainActivity.onCreate: settles matches left unfinished by a crash/kill, seeds starter data,
     * refreshes admin status, and starts automatic cloud backups when data changes.
     */
    fun onAppStart() {
        if (settledUnfinished.compareAndSet(false, true)) {
            matchQueue.trySend { matchRwds.settleUnfinished() }
            matchQueue.trySend { pvpRwds.forfeitAbandoned() }
        }
        appScope.launch {
            startSeed.seedIfEmpty()
            profileRepo.refrAdmin(settRepo.flag(SettingsRepository.OFFLINE_DEBUG) && offlineSignInAllowed)
        }
        if (autoBkpStarted.compareAndSet(false, true)) {
            cloudBkp.autoBkp(
                appScope,
                merge(cardRepo.observeCards(), coinsRepo.observeBal(), deckRepo.observeDecks(), trainerRepo.observeTrnrs()),
                WorkManager.getInstance(appContext),
            )
        }
    }

    private companion object {
        const val TAG = "AppContainer"
    }
}
