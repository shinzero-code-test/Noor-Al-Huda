package com.exapps.nooralhuda.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val NoorShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Single dark theme. Light mode is intentionally unsupported (night-first product). */@Composable
fun NoorAlHudaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NoorColorScheme,
        typography = NoorTypography,
        shapes = NoorShapes,
        content = content
    )
}
