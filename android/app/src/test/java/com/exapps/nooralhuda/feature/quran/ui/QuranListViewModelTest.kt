package com.exapps.nooralhuda.feature.quran.ui

import app.cash.turbine.test
import com.exapps.nooralhuda.MainDispatcherRule
import com.exapps.nooralhuda.core.data.prefs.PendingEmailStore
import com.exapps.nooralhuda.feature.quran.domain.QuranRepository
import com.exapps.nooralhuda.feature.quran.domain.Surah
import com.exapps.nooralhuda.feature.quran.domain.Verse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FakeQuranRepository(
    var surahList: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opener", 7, "meccan"),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", 4, "meccan")
    ),
    var refreshResult: Result<Unit> = Result.success(Unit)
) : QuranRepository {
    override val surahs = MutableStateFlow(surahList)
    override suspend fun refreshSurahs(): Result<Unit> = refreshResult
    override suspend fun surahVerses(surahId: Int, expectedCount: Int): Result<List<Verse>> =
        Result.success(emptyList())
}

class FakePendingEmailStore : PendingEmailStore {
    var email: String? = null
    var lastRead: Int? = null
    override suspend fun setPendingEmail(email: String?) {
        this.email = email
    }
    override suspend fun pendingEmail(): String? = email
    override suspend fun setLastReadSurah(surahId: Int) {
        lastRead = surahId
    }
    override suspend fun lastReadSurahId(): Int? = lastRead
}

class QuranListViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `query filters by transliteration`() = runTest {
        val vm = QuranListViewModel(FakeQuranRepository(), FakePendingEmailStore())
        vm.onQueryChange("ikhlas")
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.surahs.size)
            assertEquals(112, state.surahs.first().id)
        }
    }

    @Test
    fun `query filters by arabic`() = runTest {
        val vm = QuranListViewModel(FakeQuranRepository(), FakePendingEmailStore())
        vm.onQueryChange("الفاتحة")
        vm.uiState.test {
            assertEquals(1, awaitItem().surahs.size)
        }
    }

    @Test
    fun `blank query shows all`() = runTest {
        val vm = QuranListViewModel(FakeQuranRepository(), FakePendingEmailStore())
        vm.uiState.test {
            assertEquals(2, awaitItem().surahs.size)
        }
    }
}
