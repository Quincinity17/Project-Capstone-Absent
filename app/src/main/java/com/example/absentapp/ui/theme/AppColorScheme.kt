package com.example.absentapp.ui.theme

import androidx.compose.material.ButtonColors
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColorScheme(
    val primaryBackground: Color,
    val secondaryBackground: Color,

    val navbarUnselected: Color,
    val navbarSelected: Color,

    val primaryText: Color,
    val secondaryText: Color,
    val textFieldBackground: Color,
    val gradientDisabledButtonStart: Color,
    val gradientDisabledButtonEnd: Color,

    val checkedThumbColor: Color,
    val checkedTrackColor: Color,

    val primaryButtonColors: Color,

    val strokeButton: Color,

    val iconBgColorCheckIn: Color,
    val iconBgColorCheckOut: Color,

    val iconColorCheckIn: Color,
    val iconColorCheckOut: Color,








    val example: Color,


    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val neutral: Color,
    val bannerZonaMasuk: Color,
    val bannerZonaLuar: Color,
)

val LightAppColors = AppColorScheme(
    primaryBackground = Color(0xFFFFFFFF),
    secondaryBackground = Color(0xFFF9F9F9),

    navbarUnselected = Color(0xFFBEBEBE),
    navbarSelected = Color(0xFF022D9B),
    primaryText = Color(0xFF2C2C2C),
    secondaryText = Color(0xFFA6A6A6),
    textFieldBackground = Color(0xFFF2F2F2),
    gradientDisabledButtonStart = Color(0xFFD8D6D6),
    gradientDisabledButtonEnd = Color(0xFFC2C2C2),

    checkedThumbColor = Color(0xFF022D9B),
    checkedTrackColor = Color(0xFFA8B7DF),

    primaryButtonColors = Color(0xFF022D9B),

    strokeButton = Color(0xFFD5D5D5),

    iconBgColorCheckIn = Color(0xFFF0FCFA),
    iconBgColorCheckOut = Color(0xFFD4F1FF),

    iconColorCheckIn = Color(0xFF75C9AF),
    iconColorCheckOut = Color(0xFF01638E),






    example = Color(0xFFE91E63),

    success = Color(0xFF4CAF50),
    warning = Color(0xFFFFC107),
    danger = Color(0xFFF44336),
    info = Color(0xFF2196F3),
    neutral = Color(0xFF757575),
    bannerZonaMasuk = Color(0xFF01CCAD),
    bannerZonaLuar = Color(0xFFFF7043),

    )

val DarkAppColors = AppColorScheme(
    primaryBackground = Color(0xFF121212),
    secondaryBackground = Color(0xFF1D1D1D),

    navbarUnselected = Color(0xFF535353),
    navbarSelected = Color(0xFF0947E3),
    primaryText = Color(0xFFC8C8C8),
    secondaryText = Color(0xFF646464),
    textFieldBackground = Color(0xFF373636),
    gradientDisabledButtonStart = Color(0xFF3C3B3B),
    gradientDisabledButtonEnd = Color(0xFF525252),

    checkedThumbColor = Color(0xFF0F4EEE),
    checkedTrackColor = Color(0xFF6C7A9C),

    primaryButtonColors = Color(0xFF0947E3),
    strokeButton = Color(0xFF696868),

    iconBgColorCheckIn = Color(0xFF4F6C52),
    iconBgColorCheckOut = Color(0xFF1C4053),

    iconColorCheckIn = Color(0xFF00B57D),
    iconColorCheckOut = Color(0xFF1493CB),







    example = Color(0xFF8BC34A),




    success = Color(0xFFFFD54F),
    warning = Color(0xFFFFD54F),
    danger = Color(0xFFE57373),
    info = Color(0xFF64B5F6),
    neutral = Color(0xFFBDBDBD),
    bannerZonaMasuk = Color(0xFF00BFA5),
    bannerZonaLuar = Color(0xFFFF8A65),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
