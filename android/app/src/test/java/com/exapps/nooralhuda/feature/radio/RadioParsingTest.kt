package com.exapps.nooralhuda.feature.radio

import com.exapps.nooralhuda.feature.radio.domain.Mp3QuranRadiosResponse
import com.exapps.nooralhuda.feature.radio.domain.STATIC_FALLBACK_STATIONS
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadioParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `directory response parses and tolerates extra fields`() {
        val raw = """
            {"radios":[
              {"id":1,"name":"إبراهيم الأخضر","url":"https://backup.qurango.net/radio/ibrahim_alakdar","recent_date":"2020-04-25"},
              {"id":2,"name":"أبو بكر الشاطري","url":"https://backup.qurango.net/radio/shatri"}
            ]}
        """.trimIndent()
        val parsed = json.decodeFromString<Mp3QuranRadiosResponse>(raw)
        assertEquals(2, parsed.radios.size)
        assertEquals(1, parsed.radios[0].id)
        assertTrue(parsed.radios[0].url.startsWith("https://"))
    }

    @Test
    fun `fallback stations are unique with https streams`() {
        assertEquals(3, STATIC_FALLBACK_STATIONS.size)
        val ids = STATIC_FALLBACK_STATIONS.map { it.id }
        assertEquals(3, ids.toSet().size)
        assertTrue(
            STATIC_FALLBACK_STATIONS.all {
                it.url.startsWith("https://") && it.name.isNotBlank()
            }
        )
    }
}
