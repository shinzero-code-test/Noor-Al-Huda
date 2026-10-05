package com.exapps.nooralhuda.core.data.prefs

/** Testable seam over read-position and email-link prefs. */
interface PendingEmailStore {
    suspend fun setPendingEmail(email: String?)
    suspend fun pendingEmail(): String?
    suspend fun setLastReadSurah(surahId: Int)
}
