package com.exapps.nooralhuda.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Transcribed from Stitch mockups (viewed 2026-10-05): deep warm-black canvas,
// metallic gold primary, warm parchment text. No hardcoded colours in screens.
// EVERY M3 slot is set — any default left behind renders M3 baseline purple.
val NoorBackground = Color(0xFF0D0C0A)
val NoorSurface = Color(0xFF141310)
val NoorSurfaceHigh = Color(0xFF1C1A16)
val NoorOverlay = Color(0xFF26231E)
val NoorGold = Color(0xFFD4AF37)
val NoorGoldBright = Color(0xFFE5C158)
val NoorGoldPale = Color(0xFFF3E5AB)
val NoorGoldDeep = Color(0xFF997A15)
val NoorEmerald = Color(0xFF1A6B3C)
val NoorEmeraldBright = Color(0xFF2E9E5B)
val NoorText = Color(0xFFECE5D8)
val NoorTextDim = Color(0xFFA89F91)
val NoorTextFaint = Color(0xFF5E574D)
val NoorError = Color(0xFFE07856)
val NoorErrorDark = Color(0xFF93000A)
val NoorChipActive = Color(0xFF3A2C10)

val NoorColorScheme = darkColorScheme(
    primary = NoorGold,
    onPrimary = NoorBackground,
    primaryContainer = NoorGold,
    onPrimaryContainer = NoorBackground,
    inversePrimary = NoorGoldDeep,
    secondary = NoorGoldBright,
    onSecondary = NoorBackground,
    secondaryContainer = NoorChipActive,
    onSecondaryContainer = NoorGoldPale,
    tertiary = NoorEmeraldBright,
    onTertiary = NoorBackground,
    tertiaryContainer = NoorEmerald,
    onTertiaryContainer = NoorGoldPale,
    background = NoorBackground,
    onBackground = NoorText,
    surface = NoorSurface,
    onSurface = NoorText,
    surfaceVariant = NoorSurfaceHigh,
    onSurfaceVariant = NoorTextDim,
    surfaceTint = NoorGold,
    inverseSurface = NoorText,
    inverseOnSurface = NoorBackground,
    error = NoorError,
    onError = NoorBackground,
    errorContainer = NoorErrorDark,
    onErrorContainer = NoorText,
    surfaceBright = NoorOverlay,
    surfaceDim = NoorBackground,
    surfaceContainerLowest = NoorBackground,
    surfaceContainerLow = NoorSurface,
    surfaceContainer = NoorSurfaceHigh,
    surfaceContainerHigh = NoorOverlay,
    surfaceContainerHighest = NoorOverlay,
    outline = NoorTextFaint,
    outlineVariant = NoorTextDim,
    scrim = Color.Black
)
