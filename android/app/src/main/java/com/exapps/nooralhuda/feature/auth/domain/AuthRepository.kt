package com.exapps.nooralhuda.feature.auth.domain

data class NoorUser(
    val uid: String,
    val email: String?,
    val isAnonymous: Boolean,
    val emailVerified: Boolean
)

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object EmailInUse : AuthError
    data object WeakPassword : AuthError
    data object UserNotFound : AuthError
    data object Network : AuthError
    data object TooManyRequests : AuthError
    data object LinkExpired : AuthError
    data object RequiresRecentLogin : AuthError
    data class Unknown(val message: String?) : AuthError
}

interface AuthRepository {
    /** Null = signed out. Backed by FirebaseAuth.AuthStateListener. */
    val authState: kotlinx.coroutines.flow.StateFlow<NoorUser?>
    suspend fun currentIdToken(): String?
    suspend fun signIn(email: String, password: String): Result<NoorUser>
    suspend fun register(email: String, password: String): Result<NoorUser>
    suspend fun signInAnonymously(): Result<NoorUser>
    /** Raw Google ID token (Credential Manager wiring lands with SHA-1/OAuth config). */
    suspend fun signInWithGoogleIdToken(idToken: String): Result<NoorUser>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun sendEmailLink(email: String): Result<Unit>
    suspend fun completeEmailLink(emailLink: String, email: String): Result<NoorUser>
    suspend fun isEmailLink(emailLink: String): Boolean
    suspend fun reload(): Result<NoorUser?>
    suspend fun signOut(): Result<Unit>
}
