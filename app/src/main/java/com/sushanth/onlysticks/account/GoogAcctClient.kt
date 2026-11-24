package com.sushanth.onlysticks.account

import android.accounts.Account
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class GoogUser(val email: String, val name: String?)

sealed interface DriveAccess {
    data class Granted(val token: String) : DriveAccess
    data class NeedsConsent(val intent: PendingIntent) : DriveAccess
}

class GoogAcctClient(context: Context, private val webClientId: String) {
    private val appContext = context.applicationContext

    val isConfigured: Boolean get() = webClientId.isNotBlank()

    suspend fun signIn(act: Activity): GoogUser {
        val opt = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(opt).build()
        val credential = CredentialManager.create(act).getCredential(act, request).credential
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            throw IllegalStateException("unexpected credential type")
        }
        val goog = GoogleIdTokenCredential.createFrom(credential.data)
        return GoogUser(goog.id, goog.displayName)
    }

    suspend fun signOut() {
        CredentialManager.create(appContext).clearCredentialState(ClearCredentialStateRequest())
    }

    suspend fun driveAccess(email: String): DriveAccess = access(
        Identity.getAuthorizationClient(appContext).authorize(
            AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(DRIVE_APPDATA)))
                .setAccount(Account(email, ACCOUNT_TYPE))
                .build(),
        ).await(),
    )

    fun tokenFromConsent(data: Intent?): String? =
        runCatching { Identity.getAuthorizationClient(appContext).getAuthorizationResultFromIntent(data).accessToken }.getOrNull()

    private fun access(result: AuthorizationResult): DriveAccess {
        val pend = result.pendingIntent
        return if (result.hasResolution() && pend != null) DriveAccess.NeedsConsent(pend)
        else DriveAccess.Granted(checkNotNull(result.accessToken) { "no access token" })
    }

    private companion object {
        const val DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"
        const val ACCOUNT_TYPE = "com.google"
    }
}

internal suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
