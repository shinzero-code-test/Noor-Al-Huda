package com.exapps.nooralhuda.feature.prayer.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StreaksTest {

    @Test
    fun `three consecutive days ending today`() {
        assertEquals(3, Streaks.current(listOf("2026-10-03", "2026-10-04", "2026-10-05"), "2026-10-05"))
    }

    @Test
    fun `missing today still counts through yesterday`() {
        assertEquals(2, Streaks.current(listOf("2026-10-03", "2026-10-04"), "2026-10-05"))
    }

    @Test
    fun `gap breaks the streak`() {
        assertEquals(1, Streaks.current(listOf("2026-10-01", "2026-10-05"), "2026-10-05"))
    }

    @Test
    fun `empty gives zero`() {
        assertEquals(0, Streaks.current(emptyList(), "2026-10-05"))
    }

    @Test
    fun `invalid days rejected`() {
        assertEquals(false, Streaks.isValidIsoDay("not-a-date"))
        assertEquals(false, Streaks.isValidIsoDay("2026-13-40"))
        assertEquals(true, Streaks.isValidIsoDay("2026-10-05"))
    }

    @Test
    fun `month boundary steps back correctly`() {
        assertEquals(2, Streaks.current(listOf("2026-09-30", "2026-10-01"), "2026-10-01"))
    }
}
