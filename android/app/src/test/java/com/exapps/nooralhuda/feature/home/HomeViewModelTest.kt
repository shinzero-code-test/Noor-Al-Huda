package com.exapps.nooralhuda.feature.home

import app.cash.turbine.test
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.datetime.HijriDateProvider
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import com.exapps.nooralhuda.feature.auth.domain.NoorUser
import com.exapps.nooralhuda.feature.quran.ui.FakePendingEmailStore
import com.exapps.nooralhuda.feature.quran.ui.FakeQuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FakeAuthRepository(
    var nextUserResult: Result<NoorUser> = Result.failure(NotImplementedError()),
    var nextUnitResult: Result<Unit> = Result.failure(NotImplementedError())
) : AuthRepository {
    val state = MutableStateFlow<NoorUser?>(null)
    override val authState: StateFlow<NoorUser?> = state
    override suspend fun currentIdToken(): String? = null
    override suspend fun signIn(email: String, password: String) = nextUserResult
    override suspend fun register(email: String, password: String) = nextUserResult
    override suspend fun signInAnonymously() = nextUserResult.also {
        it.onSuccess { state.value = it }
    }
    override suspend fun signInWithGoogleIdToken(idToken: String) = nextUserResult.also {
        it.onSuccess { state.value = it }
    }
    override suspend fun sendPasswordReset(email: String) = nextUnitResult
    override suspend fun sendEmailLink(email: String) = nextUnitResult
    override suspend fun completeEmailLink(emailLink: String, email: String) = nextUserResult
    override suspend fun isEmailLink(emailLink: String) = true
    override suspend fun reload() = Result.success<NoorUser?>(state.value)
    override suspend fun signOut(): Result<Unit> {
        state.value = null
        return Result.success(Unit)
    }
}

class HomeViewModelTest {

    @get:Rule
    val mainRule = com.exapps.nooralhuda.MainDispatcherRule()

    private val fakeDates = HijriDateProvider { "14 Ramadan 1447" }

    @Test
    fun `emits greeting and hijri date`() = runTest {
        val viewModel = HomeViewModel(fakeDates, FakeAuthRepository(), FakeQuranRepository(), FakePendingEmailStore())
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("14 Ramadan 1447", state.hijriDate)
        }
    }

    @Test
    fun `signed out by default`() = runTest {
        val viewModel = HomeViewModel(fakeDates, FakeAuthRepository(), FakeQuranRepository(), FakePendingEmailStore())
        viewModel.signedIn.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `morning hours greet morning`() {
        assertEquals(R.string.home_greeting_morning, HomeViewModel.greetingFor(8))
    }

    @Test
    fun `afternoon hours greet afternoon`() {
        assertEquals(R.string.home_greeting_afternoon, HomeViewModel.greetingFor(14))
    }

    @Test
    fun `night hours greet evening`() {
        assertEquals(R.string.home_greeting_evening, HomeViewModel.greetingFor(22))
    }
}
