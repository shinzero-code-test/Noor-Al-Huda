package com.exapps.nooralhuda.feature.prayer.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreaksTest {

    @Test
    fun `three consecutive days ending today`() {
        val today = LocalDate.of(2026, 10, 5)
        assertEquals(
            3,
            Streaks.current(listOf("2026-10-03", "2026-10-04", "2026-10-05"), today)
        )
    }

    @Test
    fun `missing today still counts through yesterday`() {
        val today = LocalDate.of(2026, 10, 5)
        assertEquals(2, Streaks.current(listOf("2026-10-03", "2026-10-04"), today))
    }

    @Test
    fun `gap breaks the streak`() {
        val today = LocalDate.of(2026, 10, 5)
        assertEquals(1, Streaks.current(listOf("2026-10-01", "2026-10-05"), today))
    }

    @Test
    fun `empty gives zero`() {
        assertEquals(0, Streaks.current(emptyList(), LocalDate.of(2026, 10, 5)))
    }

    @Test
    fun `invalid days rejected`() {
        assertEquals(false, Streaks.isValidIsoDay("not-a-date"))
        assertEquals(true, Streaks.isValidIsoDay("2026-10-05"))
    }
}
