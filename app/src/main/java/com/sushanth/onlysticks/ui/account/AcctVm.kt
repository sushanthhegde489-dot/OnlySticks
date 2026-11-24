package com.sushanth.onlysticks.ui.account

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.account.CloudBkp
import com.sushanth.onlysticks.account.DriveAccess
import com.sushanth.onlysticks.account.GoogAcctClient
import com.sushanth.onlysticks.account.SyncRes
import com.sushanth.onlysticks.data.backup.DriveBkpFile
import com.sushanth.onlysticks.data.repository.AcctState
import com.sushanth.onlysticks.data.repository.ProfileRepository
import com.sushanth.onlysticks.data.repository.SettingsRepository
import com.sushanth.onlysticks.data.repository.Stat
import com.sushanth.onlysticks.data.repository.StatsRepository
import com.sushanth.onlysticks.data.repository.StartSeed
import com.sushanth.onlysticks.domain.player.DispNameRes
import com.sushanth.onlysticks.ui.common.UiTxt
import com.sushanth.onlysticks.ui.common.uiTxt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DriveAction { BACKUP, RESTORE, FIRST_SYNC, PEEK }

data class AcctUiState(
    val account: AcctState? = null,
    val googConfigured: Boolean = false,
    val busy: Boolean = false,
    val message: UiTxt? = null,
    val consent: PendingIntent? = null,
    val confirmRest: Boolean = false,
    val bkpInfo: DriveBkpFile? = null,
    val confirmSignOut: Boolean = false,
    val stats: Map<Stat, Long> = emptyMap(),
    val offlineAllowed: Boolean = false,
)

class AcctVm(
    private val prof: ProfileRepository,
    private val goog: GoogAcctClient,
    private val cloud: CloudBkp,
    private val sett: SettingsRepository,
    private val offlineAllowed: Boolean,
    private val seed: StartSeed? = null,
    private val statsRepo: StatsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AcctUiState(googConfigured = goog.isConfigured, offlineAllowed = offlineAllowed))
    val state: StateFlow<AcctUiState> = _state.asStateFlow()
    private var pendAction: DriveAction? = null

    init {
        viewModelScope.launch { prof.observeAcct().collect { a -> _state.update { it.copy(account = a) } } }
        viewModelScope.launch { statsRepo.observe().collect { s -> _state.update { it.copy(stats = s) } } }
    }

    suspend fun saveName(raw: String): DispNameRes = prof.setDispName(raw)

    fun signIn(act: Activity) = work {
        val user = goog.signIn(act)
        val prevEmail = prof.observeAcct().first().googleEmail
        val isDifferent = prevEmail != null && prevEmail != user.email
        prof.setGoogEmail(user.email)
        if (prof.displayName().isNullOrEmpty() || isDifferent) {
            user.name?.let { prof.setDispName(it) }
        }
        if (isDifferent) {
            prof.markBackedUp(null)
            seed?.seedStart(forceReset = true)
        }
        _state.update { it.copy(message = uiTxt(R.string.acct_signed_in, user.email)) }
        useDrive(DriveAction.FIRST_SYNC, user.email)
    }

    fun continueOffline() = work {
        if (!offlineAllowed) return@work
        sett.setFlag(SettingsRepository.OFFLINE_DEBUG, true)
        prof.setOfflineDebug()
        seed?.seedIfEmpty()
    }

    fun signOut() = work {
        val saved = try {
            cloud.syncNow(force = true)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("Account", "backup before sign-out failed", e)
            null
        }
        if (saved == null || saved == SyncRes.NO_ACCESS) {
            _state.update { it.copy(confirmSignOut = true) }
            return@work
        }
        finishSignOut()
    }

    fun signOutAnyway() {
        _state.update { it.copy(confirmSignOut = false) }
        work { finishSignOut() }
    }

    fun staySignedIn() = _state.update { it.copy(confirmSignOut = false) }

    private suspend fun finishSignOut() {
        prof.setGoogEmail(null)
        prof.markBackedUp(null)
        sett.setFlag(SettingsRepository.OFFLINE_DEBUG, false)
        seed?.seedStart(forceReset = true)
        runCatching { goog.signOut() }
        _state.update { it.copy(message = uiTxt(R.string.acct_signed_out)) }
    }

    fun bkp() = drive(DriveAction.BACKUP)

    fun askRest() = drive(DriveAction.PEEK)

    fun restore() {
        _state.update { it.copy(confirmRest = false, bkpInfo = null) }
        drive(DriveAction.RESTORE)
    }

    fun dismissRest() {
        _state.update { it.copy(confirmRest = false, bkpInfo = null) }
        work { seed?.seedIfEmpty() }
    }

    fun onConsentRes(data: Intent?) {
        _state.update { it.copy(consent = null) }
        val action = pendAction ?: return
        pendAction = null
        val token = goog.tokenFromConsent(data)
        if (token == null) _state.update { it.copy(message = uiTxt(R.string.acct_drive_denied)) }
        else work { run(action, token) }
    }

    private fun drive(action: DriveAction) = work { useDrive(action, _state.value.account?.googleEmail) }

    private suspend fun useDrive(action: DriveAction, email: String?) {
        if (email == null) return
        when (val access = goog.driveAccess(email)) {
            is DriveAccess.Granted -> run(action, access.token)
            is DriveAccess.NeedsConsent -> {
                pendAction = action
                _state.update { it.copy(consent = access.intent) }
            }
        }
    }

    private suspend fun run(action: DriveAction, token: String) {
        when (action) {
            DriveAction.BACKUP -> {
                cloud.up(token)
                _state.update { it.copy(message = uiTxt(R.string.acct_bkp_done)) }
            }
            DriveAction.FIRST_SYNC -> {
                val found = cloud.latest(token)
                if (found != null) _state.update { it.copy(confirmRest = true, bkpInfo = found) }
                else {
                    seed?.seedIfEmpty()
                    cloud.up(token)
                }
            }
            DriveAction.PEEK -> {
                val found = cloud.latest(token)
                if (found != null) _state.update { it.copy(confirmRest = true, bkpInfo = found) }
                else _state.update { it.copy(message = uiTxt(R.string.acct_rest_none)) }
            }
            DriveAction.RESTORE -> {
                val restored = cloud.restore(token)
                _state.update { it.copy(message = uiTxt(if (restored) R.string.acct_rest_done else R.string.acct_rest_none)) }
            }
        }
    }

    private fun work(block: suspend () -> Unit) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                block()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("Account", "account action failed", e)
                _state.update { it.copy(message = uiTxt(R.string.acct_err)) }
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }
}
