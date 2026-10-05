package com.exapps.nooralhuda.feature.bookmarks.data

interface BookmarkRepository {
    suspend fun localAll(): List<Bookmark>
    suspend fun toggle(bookmark: Bookmark)
    suspend fun push(uid: String)
    suspend fun pull(uid: String)
}
