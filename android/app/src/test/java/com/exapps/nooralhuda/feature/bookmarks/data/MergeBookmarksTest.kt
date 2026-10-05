package com.exapps.nooralhuda.feature.bookmarks.data

import org.junit.Assert.assertEquals
import org.junit.Test

class MergeBookmarksTest {

    @Test
    fun `newest timestamp wins per key`() {
        val local = listOf(Bookmark(1, 1, "A", updatedAt = 200L))
        val remote = listOf(
            Bookmark(1, 1, "A", updatedAt = 100L),
            Bookmark(2, 5, "B", updatedAt = 300L)
        )
        val merged = mergeBookmarks(local, remote)
        assertEquals(2, merged.size)
        assertEquals(200L, merged.first { it.surahId == 1 }.updatedAt)
        assertEquals(300L, merged.first { it.surahId == 2 }.updatedAt)
    }

    @Test
    fun `remote-only items survive`() {
        val merged = mergeBookmarks(emptyList(), listOf(Bookmark(112, 1, "K", updatedAt = 50L)))
        assertEquals(1, merged.size)
    }

    @Test
    fun `local-only items survive`() {
        val merged = mergeBookmarks(listOf(Bookmark(112, 1, "K", updatedAt = 50L)), emptyList())
        assertEquals(1, merged.size)
    }
}
