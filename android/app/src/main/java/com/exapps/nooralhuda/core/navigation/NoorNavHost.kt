package com.exapps.nooralhuda.core.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.exapps.nooralhuda.feature.auth.ui.AuthScreen
import com.exapps.nooralhuda.feature.auth.ui.AuthViewModel
import com.exapps.nooralhuda.feature.azkar.AzkarScreen
import com.exapps.nooralhuda.feature.hadith.HadithDetailScreen
import com.exapps.nooralhuda.feature.home.HomeScreen
import com.exapps.nooralhuda.feature.prayer.PrayerScreen
import com.exapps.nooralhuda.feature.quran.ui.SurahDetailScreen
import com.exapps.nooralhuda.feature.quran.ui.QuranScreen
import com.exapps.nooralhuda.feature.radio.RadioScreen
import com.exapps.nooralhuda.feature.settings.SettingsScreen
import com.exapps.nooralhuda.core.ui.components.NoorBottomBar

private val TAB_ROUTES = listOf(
    Home::class, Quran::class, Prayer::class, Azkar::class, Radio::class, Settings::class
)

@Composable
fun NoorAppNav() {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val authViewModel: AuthViewModel = hiltViewModel(activity)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    var authDismissed by rememberSaveable { mutableStateOf(false) }

    // Launch gate: sign-in sheet on every cold start while signed out.
    // Guest counts as signed in. Dismissal lasts for this launch only.
    if (authState.user == null && !authDismissed) {
        AuthGateSheet(
            onSignedIn = { /* collector below hides the sheet */ },
            onDismiss = { authDismissed = true }
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val selectedTab = TAB_ROUTES.indexOfFirst { route ->
        destination?.hierarchy?.any { it.hasRoute(route) } == true
    }

    Scaffold(
        bottomBar = {
            if (selectedTab >= 0) {
                NoorBottomBar(
                    selectedIndex = selectedTab,
                    onSelect = { index ->
                        val route = when (index) {
                            0 -> Home
                            1 -> Quran
                            2 -> Prayer
                            3 -> Azkar
                            4 -> Radio
                            else -> Settings
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = Modifier.padding(padding)
        ) {
            composable<Home> {
                HomeScreen(
                    onOpenQuran = { navController.navigate(Quran) },
                    onOpenPrayer = { navController.navigate(Prayer) },
                    onOpenAzkar = { navController.navigate(Azkar) },
                    onOpenRadio = { navController.navigate(Radio) },
                    onOpenSurah = { navController.navigate(SurahDetail(it)) }
                )
            }
            composable<Quran> { QuranScreen(onSurahClick = { navController.navigate(SurahDetail(it)) }) }
            composable<Prayer> { PrayerScreen() }
            composable<Azkar> { AzkarScreen() }
            composable<Radio> { RadioScreen() }
            composable<Settings> { SettingsScreen() }
            composable<SurahDetail> {
                SurahDetailScreen()
            }
            composable<HadithDetail> { backStackEntry ->
                HadithDetailScreen(hadithId = backStackEntry.toRoute<HadithDetail>().hadithId)
            }
            composable<Auth> {
                AuthScreen(onSignedIn = {
                    navController.navigate(Home) {
                        popUpTo(navController.graph.findStartDestination().id)
                    }
                })
            }
        }
    }

    // Keep the sheet honest: hide the moment sign-in lands.
    LaunchedEffect(authState.user) {
        if (authState.user != null) authDismissed = true
    }
}
