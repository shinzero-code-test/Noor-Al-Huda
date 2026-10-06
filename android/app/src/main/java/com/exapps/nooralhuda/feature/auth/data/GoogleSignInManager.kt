package com.exapps.nooralhuda.feature.auth.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.exapps.nooralhuda.core.di.GoogleWebClientId
import com.exapps.nooralhuda.feature.auth.domain.AuthError
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Test seam for the Credential Manager Google flow. */
interface GoogleSignIn {
    val isAvailable: Boolean
    suspend fun requestIdToken(context: Context): Result<String>
}

/** Marker: user dismissed the account sheet — UI stays silent, no error shown. */
class GoogleDismissedException : Exception("Google sign-in dismissed")

@Singleton
class GoogleSignInManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @GoogleWebClientId private val webClientId: String
) : GoogleSignIn {

    /** False until the OAuth web client ID is configured (BuildConfig). */
    override val isAvailable: Boolean get() = webClientId.isNotBlank()

    override suspend fun requestIdToken(context: Context): Result<String> {
        if (!isAvailable) {
            return Result.failure(
                AuthException(AuthError.GoogleUnavailable, IllegalStateException("no web client id"))
            )
        }
        return try {
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            // Any Context works here — deliberately no Activity cast (dialog
            // contexts are not Activities; see the AuthScreen crash fix).
            val response = CredentialManager.create(appContext).getCredential(context, request)
            val google = GoogleIdTokenCredential.createFrom(response.credential.data)
            Result.success(google.idToken)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(GoogleDismissedException())
        } catch (e: GetCredentialException) {
            Result.failure(AuthException(AuthError.GoogleUnavailable, e))
        } catch (e: Exception) {
            Result.failure(AuthException(AuthError.Unknown(e.message), e))
        }
    }
}
