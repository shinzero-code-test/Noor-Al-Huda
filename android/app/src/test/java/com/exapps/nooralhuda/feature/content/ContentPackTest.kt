package com.exapps.nooralhuda.feature.content

import com.exapps.nooralhuda.feature.calendar.domain.STATIC_ISLAMIC_EVENTS
import com.exapps.nooralhuda.feature.dua.domain.STATIC_DUA_CATALOG
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeKind
import com.exapps.nooralhuda.feature.knowledge.domain.STATIC_KNOWLEDGE
import com.exapps.nooralhuda.feature.seerah.domain.STATIC_SEERAH_CHAPTERS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentPackTest {

    @Test
    fun `dua catalog covers all categories with valid repeats`() {
        assertEquals(13, STATIC_DUA_CATALOG.size)
        val ids = STATIC_DUA_CATALOG.map { it.id }
        assertEquals(13, ids.toSet().size)
        assertTrue(STATIC_DUA_CATALOG.all { it.repeat in 1..7 })
        assertTrue(
            STATIC_DUA_CATALOG.all {
                it.arabic.isNotBlank() && it.translation.isNotBlank() && it.source.isNotBlank()
            }
        )
        val categories = STATIC_DUA_CATALOG.map { it.category }.toSet()
        assertEquals(6, categories.size)
    }

    @Test
    fun `calendar events have valid hijri dates`() {
        assertEquals(6, STATIC_ISLAMIC_EVENTS.size)
        assertTrue(STATIC_ISLAMIC_EVENTS.all { it.hijriMonth in 1..12 && it.hijriDay in 1..30 })
    }

    @Test
    fun `seerah chapters are unique with lessons`() {
        assertEquals(6, STATIC_SEERAH_CHAPTERS.size)
        assertEquals(6, STATIC_SEERAH_CHAPTERS.map { it.id }.toSet().size)
        assertTrue(
            STATIC_SEERAH_CHAPTERS.all {
                it.summary.isNotBlank() && it.reflection.isNotBlank() && it.lessons.isNotEmpty()
            }
        )
    }

    @Test
    fun `knowledge corpus covers all five kinds`() {
        assertEquals(18, STATIC_KNOWLEDGE.size)
        assertEquals(18, STATIC_KNOWLEDGE.map { it.id }.toSet().size)
        val kinds = STATIC_KNOWLEDGE.map { it.kind }.toSet()
        assertEquals(
            setOf(
                KnowledgeKind.NAME, KnowledgeKind.RUQYAH, KnowledgeKind.FAQ,
                KnowledgeKind.EBOOK, KnowledgeKind.STREAM
            ),
            kinds
        )
        assertEquals(6, STATIC_KNOWLEDGE.count { it.kind == KnowledgeKind.NAME })
    }
}
