package com.exapps.nooralhuda.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Stitch mockups use Noto Serif / Plus Jakarta Sans as stand-ins.
// The APK ships real Amiri (display/Quran), Cairo (Arabic UI) and
// Noto Naskh Arabic (transliteration fallback) font files (Stage A1 assets);
// these families below map 1:1 onto them.
val AmiriFamily = FontFamily.Serif
val CairoFamily = FontFamily.SansSerif

// Quranic text needs ~1.9x line height so tashkeel never collides.
val NoorTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = AmiriFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AmiriFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AmiriFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CairoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CairoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CairoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

val QuranVerseStyle = TextStyle(
    fontFamily = AmiriFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 54.sp
)
