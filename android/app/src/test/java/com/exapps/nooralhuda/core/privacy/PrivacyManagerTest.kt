package com.exapps.nooralhuda.core.privacy

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PrivacyManagerTest {

    @Test
    fun `private mode blocks all network`() = runTest {
        val manager = PrivacyManager(MutableStateFlow(PrivacyModes.PRIVATE))
        assertEquals(false, manager.canSync())
        assertEquals(false, manager.canUseAi())
        assertEquals(false, manager.canUseRemoteSearch())
        assertEquals(false, manager.canUseServerStt())
    }

    @Test
    fun `full mode allows all network`() = runTest {
        val manager = PrivacyManager(MutableStateFlow(PrivacyModes.FULL))
        assertEquals(true, manager.canSync())
        assertEquals(true, manager.canUseAi())
        assertEquals(true, manager.canUseRemoteSearch())
        assertEquals(true, manager.canUseServerStt())
    }

    @Test
    fun `allowlist keeps first-party and backend hosts`() {
        val manager = PrivacyManager(MutableStateFlow(PrivacyModes.FULL))
        assertEquals(true, manager.canRequestUrl("https://apis.quran.foundation/content/api/v4/chapters"))
        assertEquals(true, manager.canRequestUrl("https://nooralhuda-admin-api.shinzero.workers.dev/api/health"))
        assertEquals(false, manager.canRequestUrl("https://example.com/track"))
    }
}
