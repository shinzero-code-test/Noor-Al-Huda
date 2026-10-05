package com.exapps.nooralhuda.feature.auth.ui

import app.cash.turbine.test
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.data.prefs.PendingEmailStore
import com.exapps.nooralhuda.feature.auth.data.AuthException
import com.exapps.nooralhuda.feature.auth.domain.AuthError
import com.exapps.nooralhuda.feature.auth.domain.NoorUser
import com.exapps.nooralhuda.core.navigation.DeepLinkBus
import com.exapps.nooralhuda.feature.home.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FakePendingEmail : PendingEmailStore {
    var email: String? = null
    override suspend fun setPendingEmail(email: String?) {
        this.email = email
    }
    override suspend fun pendingEmail(): String? = email
    override suspend fun setLastReadSurah(surahId: Int) {
    }
    override suspend fun lastReadSurahId(): Int? = null
}

class AuthViewModelTest {

    @get:Rule
    val mainRule = com.exapps.nooralhuda.MainDispatcherRule()

    private val user = NoorUser("uid1", "a@b.c", false, false)

    private fun failure(error: AuthError) =
        Result.failure<NoorUser>(AuthException(error, IllegalStateException("test")))

    @Test
    fun `invalid credentials map to invalid error string`() = runTest {
        val auth = FakeAuthRepository(nextUserResult = failure(AuthError.InvalidCredentials))
        val vm = AuthViewModel(auth, FakePendingEmail(), DeepLinkBus())
        vm.signIn("a@b.c", "wrong")
        vm.uiState.test {
            assertEquals(R.string.auth_error_invalid, awaitItem().errorRes)
        }
    }

    @Test
    fun `guest success publishes user`() = runTest {
        val auth = FakeAuthRepository(nextUserResult = Result.success(user))
        val vm = AuthViewModel(auth, FakePendingEmail(), DeepLinkBus())
        vm.continueAsGuest()
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(null, state.errorRes)
            assertEquals(user, state.user)
        }
    }

    @Test
    fun `send link stores pending email`() = runTest {
        val auth = FakeAuthRepository(nextUnitResult = Result.success(Unit))
        val prefs = FakePendingEmail()
        val vm = AuthViewModel(auth, prefs, DeepLinkBus())
        vm.sendLink("a@b.c")
        vm.uiState.test {
            assertEquals(R.string.auth_link_sent, awaitItem().infoRes)
        }
        assertEquals("a@b.c", prefs.email)
    }
}
