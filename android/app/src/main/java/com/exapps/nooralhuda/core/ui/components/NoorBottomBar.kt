package com.exapps.nooralhuda.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.exapps.nooralhuda.R

data class NoorTab(val labelRes: Int, val icon: ImageVector)

val NOOR_TABS = listOf(
    NoorTab(R.string.tab_home, Icons.Filled.Home),
    NoorTab(R.string.tab_quran, Icons.Filled.MenuBook),
    NoorTab(R.string.tab_prayer, Icons.Filled.Mosque),
    NoorTab(R.string.tab_azkar, Icons.Filled.SelfImprovement),
    NoorTab(R.string.tab_radio, Icons.Filled.Radio),
    NoorTab(R.string.tab_settings, Icons.Filled.Settings)
)

/**
 * Bottom bar transcribed from the Stitch mockups: dark bar, gold filled
 * circle behind the active icon, dim inactive icons, gold active label.
 * No M3 indicator pill — the mockups do not have one.
 */
@Composable
fun NoorBottomBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NOOR_TABS.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(role = Role.Tab, onClick = { onSelect(index) })
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = stringResource(tab.labelRes),
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = stringResource(tab.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
