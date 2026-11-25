// The player's profile: display name, Google account link, admin and tutorial flags.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.repository

import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.PlrProfEntity
import com.sushanth.onlysticks.domain.admin.AdminPolicy
import com.sushanth.onlysticks.domain.player.DispName
import com.sushanth.onlysticks.domain.player.DispNameRes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Snapshot of profile fields the UI cares about. */
data class AcctState(
    val displayName: String?,
    val googleEmail: String?,
    val lastBackupAt: Long?,
    val isAdmin: Boolean = false,
    val tutorialDone: Boolean = false,
)

/** Reads and updates the single profile row. [admins] is the allow-list from BuildConfig.ADMIN_EMAILS. */
class ProfileRepository(db: AppDatabase, private val admins: Set<String> = emptySet()) {
    private val dao = db.plrProfDao()

    /** Live profile state (fields are null/false before a profile exists). */
    fun observeAcct(): Flow<AcctState> =
        dao.observe().map { AcctState(it?.displayName, it?.googleEmail, it?.lastBackupAt, it?.isAdmin == true, it?.tutorialDone == true) }

    /** Validates and saves a new display name. Returns the validation result. */
    suspend fun setDispName(raw: String): DispNameRes {
        val result = DispName.check(raw)
        if (result is DispNameRes.Valid) update { it.copy(displayName = result.name) }
        return result
    }

    suspend fun displayName(): String? = dao.get()?.displayName

    /** Links (or unlinks, with null) a Google account and recalculates admin status. */
    suspend fun setGoogEmail(email: String?) = update { it.copy(googleEmail = email, isAdmin = AdminPolicy.isAdmin(email, admins)) }

    suspend fun isAdmin(): Boolean = dao.get()?.isAdmin == true

    /**
     * Re-checks admin status (e.g. after the allow-list changed in a new build).
     * [offline] treats the special offline debug account as signed in.
     */
    suspend fun refrAdmin(offline: Boolean) {
        val row = dao.get() ?: return
        val admin = AdminPolicy.isAdmin(row.googleEmail ?: if (offline) OFFLINE_DEBUG_ID else null, admins)
        if (admin != row.isAdmin) dao.upsert(row.copy(isAdmin = admin))
    }

    /** Switches to the offline debug account (debug builds only). */
    suspend fun setOfflineDebug() = update { it.copy(googleEmail = null, isAdmin = AdminPolicy.isAdmin(OFFLINE_DEBUG_ID, admins)) }

    suspend fun setTutDone(done: Boolean) = update { it.copy(tutorialDone = done) }

    suspend fun markBackedUp(at: Long?) = update { it.copy(lastBackupAt = at) }

    /** Applies [change] to the profile, creating an empty profile first if needed. */
    private suspend fun update(change: (PlrProfEntity) -> PlrProfEntity) {
        dao.upsert(change(dao.get() ?: PlrProfEntity(LOCAL_ID, "")))
    }

    companion object {
        private const val LOCAL_ID = "local"
        /** Pseudo-email used for offline debug sign-in; add it to ADMIN_EMAILS to get admin tools offline. */
        const val OFFLINE_DEBUG_ID = "offline@debug.local"
    }
}
