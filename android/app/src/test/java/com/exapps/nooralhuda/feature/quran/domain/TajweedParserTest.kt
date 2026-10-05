package com.exapps.nooralhuda.feature.quran.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TajweedParserTest {

    private val base = Color.White
    private val marker = Color.Yellow

    @Test
    fun `plain text without markup passes through`() {
        val out = TajweedParser.toAnnotatedString(null, "plain", base, marker)
        assertEquals("plain", out.text)
        assertTrue(out.spanStyles.isEmpty())
    }

    @Test
    fun `tajweed span keeps word and colors it`() {
        val tajweed = "قُلْ هُوَ <tajweed class=qalaqah>د</tajweed>ٌ"
        val out = TajweedParser.toAnnotatedString(tajweed, "fallback", base, marker)
        assertTrue(out.text.contains("د"))
        assertTrue(out.spanStyles.isNotEmpty())
    }

    @Test
    fun `verse marker becomes bracketed digits`() {
        val out = TajweedParser.toAnnotatedString("<span class=end>١</span>", "", base, marker)
        assertEquals(" ﴿١﴾", out.text)
    }

    @Test
    fun `unknown tags are stripped with text preserved`() {
        val out = TajweedParser.toAnnotatedString("a<foo>bar</foo>b", "", base, marker)
        assertEquals("abarb", out.text)
    }

    @Test
    fun `translation footnotes are stripped`() {
        val raw = "In the name of Allāh.<sup foot_note=254011>1</sup> the Entirely Merciful."
        assertEquals(
            "In the name of Allāh.1 the Entirely Merciful.",
            TajweedParser.cleanTranslation(raw)
        )
    }

    @Test
    fun `unknown tajweed class falls back to base color`() {
        val out = TajweedParser.toAnnotatedString(
            "<tajweed class=mystery>x</tajweed>", "", base, marker
        )
        val style: SpanStyle = out.spanStyles.first().item
        assertEquals(base, style.color)
    }

    @Test
    fun `legend covers the three documented classes`() {
        val names = TajweedParser.legend.map { it.first }
        assertEquals(listOf("Ghunnah", "Madd", "Qalqalah"), names)
    }
}
