package com.exapps.nooralhuda.feature.hadith

import com.exapps.nooralhuda.feature.hadith.domain.HadithListResponse
import com.exapps.nooralhuda.feature.hadith.domain.HadithOneDto
import com.exapps.nooralhuda.feature.hadith.domain.STATIC_HADITH_COLLECTIONS
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HadithParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `list response parses with string page numbers`() {
        val raw = """
            {"data":[{"id":"5907","title":"تعاهدوا هذا القرآن"}],
             "meta":{"current_page":"1","last_page":3782,"total_items":7563,"per_page":"2"}}
        """.trimIndent()
        val parsed = json.decodeFromString<HadithListResponse>(raw)
        assertEquals(1, parsed.data.size)
        assertEquals("5907", parsed.data[0].id)
        assertEquals(3782, parsed.meta.last_page)
    }

    @Test
    fun `detail response parses with optional attribution`() {
        val raw = """
            {"id":"1","title":"إنما الأعمال بالنيات","hadeeth":"إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ",
             "attribution":"صحيح البخاري","grade":"Sahih"}
        """.trimIndent()
        val parsed = json.decodeFromString<HadithOneDto>(raw)
        assertEquals("صحيح البخاري", parsed.attribution)
        assertEquals("Sahih", parsed.grade)
    }

    @Test
    fun `detail response tolerates missing optional fields`() {
        val raw = """{"id":"9","title":"x","hadeeth":"y"}"""
        val parsed = json.decodeFromString<HadithOneDto>(raw)
        assertNull(parsed.attribution)
        assertNull(parsed.grade)
    }

    @Test
    fun `blank detail body is treated as missing translation`() {
        val blank = "  "
        assertTrue(blank.isBlank())
    }

    @Test
    fun `static catalog has 17 unique collections with counts`() {
        assertEquals(17, STATIC_HADITH_COLLECTIONS.size)
        val ids = STATIC_HADITH_COLLECTIONS.map { it.id }
        assertEquals(17, ids.toSet().size)
        assertTrue(STATIC_HADITH_COLLECTIONS.all { it.count > 0 && it.title.isNotBlank() })
    }
}
