package com.exapps.nooralhuda.feature.auth.data

import com.exapps.nooralhuda.feature.auth.domain.AuthError
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import com.exapps.nooralhuda.feature.auth.domain.NoorUser
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseNetworkException
import com.google.firebase.auth.FirebaseTooManyRequestsException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    private val _authState = MutableStateFlow(auth.currentUser?.toNoorUser())

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser?.toNoorUser()
        }
    }

    override val authState: StateFlow<NoorUser?> = _authState.asStateFlow()

    override suspend fun currentIdToken(): String? = try {
        auth.currentUser?.getIdToken(false)?.await()?.token
    } catch (_: Exception) {
        null
    }

    override suspend fun signIn(email: String, password: String): Result<NoorUser> =
        runAuth { auth.signInWithEmailAndPassword(email.trim(), password).await().user!!.toNoorUser() }

    override suspend fun register(email: String, password: String): Result<NoorUser> =
        runAuth { auth.createUserWithEmailAndPassword(email.trim(), password).await().user!!.toNoorUser() }

    override suspend fun signInAnonymously(): Result<NoorUser> =
        runAuth { auth.signInAnonymously().await().user!!.toNoorUser() }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<NoorUser> =
        runAuth {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await().user!!.toNoorUser()
        }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runAuth { auth.sendPasswordResetEmail(email.trim()).await() }

    override suspend fun sendEmailLink(email: String): Result<Unit> = runAuth {
        val settings = ActionCodeSettings.newBuilder()
            .setUrl("https://nooralhuda-2026.firebaseapp.com/finishSignIn")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.exapps.nooralhuda", true, null)
            .build()
        auth.sendSignInLinkToEmail(email.trim(), settings).await()
    }

    override suspend fun completeEmailLink(emailLink: String, email: String): Result<NoorUser> =
        runAuth { auth.signInWithEmailLink(email.trim(), emailLink).await().user!!.toNoorUser() }

    override suspend fun isEmailLink(emailLink: String): Boolean = auth.isSignInWithEmailLink(emailLink)

    override suspend fun reload(): Result<NoorUser?> = runAuth {
        auth.currentUser?.reload()?.await()
        auth.currentUser?.toNoorUser()
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
    }

    private suspend fun <T> runAuth(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: FirebaseAuthWeakPasswordException) {
        Result.failure(mapError(AuthError.WeakPassword, e))
    } catch (e: FirebaseAuthUserCollisionException) {
        Result.failure(mapError(AuthError.EmailInUse, e))
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.failure(mapError(AuthError.InvalidCredentials, e))
    } catch (e: FirebaseAuthInvalidUserException) {
        Result.failure(mapError(AuthError.UserNotFound, e))
    } catch (e: FirebaseAuthActionCodeException) {
        Result.failure(mapError(AuthError.LinkExpired, e))
    } catch (e: FirebaseTooManyRequestsException) {
        Result.failure(mapError(AuthError.TooManyRequests, e))
    } catch (e: FirebaseNetworkException) {
        Result.failure(mapError(AuthError.Network, e))
    } catch (e: Exception) {
        Result.failure(mapError(AuthError.Unknown(e.message), e))
    }

    private fun mapError(error: AuthError, cause: Throwable): AuthException =
        AuthException(error, cause)

    private fun com.google.firebase.auth.FirebaseUser.toNoorUser() = NoorUser(
        uid = uid,
        email = email,
        isAnonymous = isAnonymous,
        emailVerified = isEmailVerified
    )
}

class AuthException(val error: AuthError, cause: Throwable) : Exception(cause)
