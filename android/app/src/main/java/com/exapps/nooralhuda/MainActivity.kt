package com.exapps.nooralhuda

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.exapps.nooralhuda.core.navigation.DeepLinkBus
import com.exapps.nooralhuda.core.navigation.NoorAppNav
import com.exapps.nooralhuda.core.ui.theme.NoorAlHudaTheme
import com.exapps.nooralhuda.core.sync.SyncScheduler
import com.exapps.nooralhuda.feature.auth.ui.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deepLinkBus: DeepLinkBus

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SyncScheduler.schedulePeriodic(this)
        forwardEmailLink(intent)
        setContent {
            NoorAlHudaTheme {
                NoorAppNav()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        forwardEmailLink(intent)
    }

    private fun forwardEmailLink(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val link = intent.dataString ?: return
        // Complete on the activity VM; AuthScreen observes the shared result state.
        authViewModel.consumeEmailLink(link)
        deepLinkBus.emit(link)
    }
}
