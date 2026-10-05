package com.exapps.nooralhuda.core.data.prefs

/** Testable seam over the pending email-link address. */
interface PendingEmailStore {
    suspend fun setPendingEmail(email: String?)
    suspend fun pendingEmail(): String?
}
