package com.exapps.nooralhuda.feature.home

import app.cash.turbine.test
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.datetime.HijriDateProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {

    private val fakeDates = HijriDateProvider { "14 Ramadan 1447" }

    @Test
    fun `emits greeting and hijri date`() = runTest {
        val viewModel = HomeViewModel(fakeDates)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("14 Ramadan 1447", state.hijriDate)
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
