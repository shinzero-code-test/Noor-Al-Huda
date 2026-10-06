package com.exapps.nooralhuda.feature.khatm.domain

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow

/** A reading group. Members and progress resolve via KhatmRepository. */
@Immutable
data class KhatmGroup(
    val id: String,
    val name: String,
    val openJoin: Boolean,
    val createdBy: String,
    val inviteCode: String? = null
)

/** One member slot: doc ID is "<groupId>__<uid>". */
@Immutable
data class KhatmMember(
    val groupId: String,
    val uid: String,
    val displayName: String,
    val completedPages: Set<Int>
)

interface KhatmRepository {
    /** Groups the caller belongs to (live). */
    fun myGroups(): Flow<List<KhatmGroup>>

    /** Open groups for discovery (live). */
    fun openGroups(): Flow<List<KhatmGroup>>

    /** Members of one group (live, member-only). */
    fun members(groupId: String): Flow<List<KhatmMember>>

    /** Create a group (open or closed) and slot yourself. */
    suspend fun createGroup(name: String, openJoin: Boolean): Result<Unit>

    /** Join via invite code (closed) — resolves code → group → slot. */
    suspend fun joinWithCode(code: String): Result<KhatmGroup>

    /** Join an open group directly. */
    suspend fun joinOpen(groupId: String): Result<Unit>

    /** Mark one page done on your own slot (idempotent). */
    suspend fun markPageDone(groupId: String, page: Int): Result<Unit>

    /** Leave a group (deletes your slot). */
    suspend fun leave(groupId: String): Result<Unit>

    /** Creator removes a member. */
    suspend fun removeMember(groupId: String, uid: String): Result<Unit>

    /** Invite code for a group you created (null when open or not yours). */
    suspend fun inviteCodeFor(groupId: String): String?
}

/** Group progress = union of member pages over the 604-page mushaf. */
fun khatmProgress(members: List<KhatmMember>, totalPages: Int = 604): KhatmProgress {
    val done = members.flatMap { it.completedPages }.toSet()
    return KhatmProgress(donePages = done.size, totalPages = totalPages)
}

@Immutable
data class KhatmProgress(val donePages: Int, val totalPages: Int) {
    val percent: Int get() = if (totalPages == 0) 0 else (donePages * 100 / totalPages).coerceIn(0, 100)
}

/** First unclaimed page, or null when the khatm is complete. */
fun nextUnclaimedPage(members: List<KhatmMember>, totalPages: Int = 604): Int? {
    val done = members.flatMap { it.completedPages }.toSet()
    return (1..totalPages).firstOrNull { it !in done }
}
