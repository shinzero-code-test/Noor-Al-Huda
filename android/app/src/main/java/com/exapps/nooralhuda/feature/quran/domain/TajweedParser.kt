package com.exapps.nooralhuda.feature.quran.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

/**
 * Parses Quran Foundation tajweed markup:
 * `<tajweed class=ham_wasl>…</tajweed>` spans and `<span class=end>١</span>`
 * verse markers. Unknown tags are stripped, text preserved.
 * Pure function — fully unit-tested.
 */
object TajweedParser {

    private val spanRegex =
        Regex("<tajweed class=([a-z_0-9]+)>(.*?)</tajweed>|<span class=end>(.*?)</span>")

    // Reviewed against the mockup legend: emerald Ghunnah, amber Madd, sapphire Qalqalah.
    private val classColors = mapOf(
        "ghunnah" to Color(0xFF2E9E5B),
        "madd_normal" to Color(0xFFE5C158),
        "madd_permissible" to Color(0xFFE5C158),
        "madd_necessary" to Color(0xFFE5C158),
        "qalaqah" to Color(0xFF6B9BD1),
        "ikhfa" to Color(0xFF6B9BD1),
        "ikhfa_shafawi" to Color(0xFF6B9BD1),
        "iqlab" to Color(0xFF6B9BD1),
        "idgham_ghunnah" to Color(0xFF2E9E5B),
        "idgham_no_ghunnah" to Color(0xFF2E9E5B),
        "idgham_shafawi" to Color(0xFF2E9E5B),
        "ham_wasl" to Color(0xFF8A8175),
        "silent" to Color(0xFF8A8175),
        "laam_shamsiyah" to Color(0xFF8A8175)
    )

    val legend: List<Pair<String, Color>> = listOf(
        "Ghunnah" to Color(0xFF2E9E5B),
        "Madd" to Color(0xFFE5C158),
        "Qalqalah" to Color(0xFF6B9BD1)
    )

    fun toAnnotatedString(
        tajweed: String?,
        plain: String,
        baseColor: Color,
        markerColor: Color
    ): AnnotatedString {
        if (tajweed.isNullOrBlank()) return AnnotatedString(plain)
        return buildAnnotatedString {
            var cursor = 0
            for (match in spanRegex.findAll(tajweed)) {
                if (match.range.first > cursor) {
                    append(tajweed.substring(cursor, match.range.first).stripTags())
                }
                val cls = match.groups[1]?.value
                val word = match.groups[2]?.value
                val marker = match.groups[3]?.value
                when {
                    word != null -> {
                        val color = classColors[cls] ?: baseColor
                        pushStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold))
                        append(word.stripTags())
                        pop()
                    }
                    marker != null -> {
                        pushStyle(SpanStyle(color = markerColor, fontWeight = FontWeight.Bold))
                        append(" ﴿$marker﴾")
                        pop()
                    }
                }
                cursor = match.range.last + 1
            }
            if (cursor < tajweed.length) append(tajweed.substring(cursor).stripTags())
        }
    }

    private fun String.stripTags(): String = replace(Regex("<[^>]+>"), "")
}
