package com.exapps.nooralhuda.feature.khatm

import com.exapps.nooralhuda.feature.khatm.domain.KhatmMember
import com.exapps.nooralhuda.feature.khatm.domain.khatmProgress
import com.exapps.nooralhuda.feature.khatm.domain.nextUnclaimedPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KhatmProgressTest {

    private fun member(uid: String, vararg pages: Int) =
        KhatmMember("g", uid, uid, pages.toSet())

    @Test
    fun `union counts shared pages once`() {
        val progress = khatmProgress(
            listOf(member("a", 1, 2, 3), member("b", 3, 4))
        )
        assertEquals(4, progress.donePages)
        assertEquals(604, progress.totalPages)
        assertEquals(0, progress.percent)
    }

    @Test
    fun `percent scales to one hundred`() {
        val progress = khatmProgress(listOf(member("a", *(1..302).toList().toIntArray())))
        assertEquals(50, progress.percent)
        val full = khatmProgress(listOf(member("a", *(1..604).toList().toIntArray())))
        assertEquals(100, full.percent)
    }

    @Test
    fun `next page skips claimed`() {
        assertEquals(4, nextUnclaimedPage(listOf(member("a", 1, 2, 3))))
        assertEquals(1, nextUnclaimedPage(emptyList()))
        assertNull(nextUnclaimedPage(listOf(member("a", *(1..604).toList().toIntArray()))))
    }

    @Test
    fun `empty group is zero`() {
        val progress = khatmProgress(emptyList())
        assertEquals(0, progress.donePages)
        assertEquals(0, progress.percent)
    }
}
