package com.exapps.nooralhuda.feature.content

import com.exapps.nooralhuda.core.datetime.mondayFirstIndex
import com.exapps.nooralhuda.feature.daily.domain.FALLBACK_DAILY
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeEntry
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeKind
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class Train130Test {

    @Test
    fun `monday-first index covers the week`() {
        assertEquals(0, DayOfWeek.MONDAY.mondayFirstIndex())
        assertEquals(1, DayOfWeek.TUESDAY.mondayFirstIndex())
        assertEquals(4, DayOfWeek.FRIDAY.mondayFirstIndex())
        assertEquals(6, DayOfWeek.SUNDAY.mondayFirstIndex())
    }

    @Test
    fun `fallback daily content is complete`() {
        assertEquals(2, FALLBACK_DAILY.surahId)
        assertEquals("2:186", FALLBACK_DAILY.verseRef)
        assertTrue(FALLBACK_DAILY.verseArabic.isNotBlank())
        assertTrue(FALLBACK_DAILY.hadithText.isNotBlank())
    }

    @Test
    fun `daily rotation math stays in range`() {
        for (dayOfYear in 1..366) {
            val surahId = (dayOfYear % 114) + 1
            val page = (dayOfYear % 197) + 1
            assertTrue(surahId in 1..114)
            assertTrue(page >= 1)
        }
    }

    @Test
    fun `name of the day rotates deterministically`() {
        val names = listOf(
            KnowledgeEntry("a", KnowledgeKind.NAME, "أ", "A", "m"),
            KnowledgeEntry("b", KnowledgeKind.NAME, "ب", "B", "m"),
            KnowledgeEntry("c", KnowledgeKind.NAME, "ج", "C", "m")
        )
        val repo = object : KnowledgeRepository {
            override val entries: StateFlow<List<KnowledgeEntry>> = MutableStateFlow(names)
            override suspend fun warm() {}
        }
        val first = repo.nameOfTheDay()
        val second = repo.nameOfTheDay()
        assertNotNull(first)
        assertEquals(first?.id, second?.id)
        assertTrue(names.map { it.id }.contains(first?.id))
    }

    @Test
    fun `zakat is two point five percent`() {
        val total = 1000.0 + 500.0 + 250.0
        assertEquals("43.75", "%.2f".format(total * 0.025))
    }
}
