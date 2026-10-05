package com.exapps.nooralhuda.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.feature.auth.ui.AuthScreen
import com.exapps.nooralhuda.feature.azkar.AzkarScreen
import com.exapps.nooralhuda.feature.hadith.HadithDetailScreen
import com.exapps.nooralhuda.feature.home.HomeScreen
import com.exapps.nooralhuda.feature.prayer.PrayerScreen
import com.exapps.nooralhuda.feature.quran.ui.QuranScreen
import com.exapps.nooralhuda.feature.quran.ui.SurahDetailScreen
import com.exapps.nooralhuda.feature.radio.RadioScreen
import com.exapps.nooralhuda.feature.settings.SettingsScreen

private data class Tab(val route: Any, val labelRes: Int, val icon: ImageVector)

private val TABS = listOf(
    Tab(Home, R.string.tab_home, Icons.Filled.Home),
    Tab(Quran, R.string.tab_quran, Icons.Filled.MenuBook),
    Tab(Prayer, R.string.tab_prayer, Icons.Filled.Mosque),
    Tab(Azkar, R.string.tab_azkar, Icons.Filled.SelfImprovement),
    Tab(Radio, R.string.tab_radio, Icons.Filled.Radio),
    Tab(Settings, R.string.tab_settings, Icons.Filled.Settings)
)

@Composable
fun NoorAppNav() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { NoorBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = Modifier.padding(padding)
        ) {
            composable<Home> { HomeScreen(onSignInClick = { navController.navigate(Auth) }) }
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
}

@Composable
private fun NoorBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    NavigationBar {
        TABS.forEach { tab ->
            val selected = destination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}
