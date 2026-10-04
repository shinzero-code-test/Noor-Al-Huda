package com.exapps.nooralhuda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.exapps.nooralhuda.core.navigation.NoorAppNav
import com.exapps.nooralhuda.core.ui.theme.NoorAlHudaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoorAlHudaTheme {
                NoorAppNav()
            }
        }
    }
}
