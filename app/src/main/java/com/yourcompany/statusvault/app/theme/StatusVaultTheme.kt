package com.yourcompany.statusvault.app.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val BrandGreen = Color(0xFF25D366)
private val BrandGreenDark = Color(0xFF128C4A)
private val BrandMint = Color(0xFFDFF8E7)
private val AppBackground = Color(0xFFF7FAF8)
private val AppSurface = Color(0xFFFFFFFF)
private val AppSurfaceVariant = Color(0xFFF1F5F2)
private val AppOutline = Color(0xFFE2ECE6)
private val AppText = Color(0xFF111B16)
private val AppTextSecondary = Color(0xFF66756C)

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandMint,
    onPrimaryContainer = BrandGreenDark,
    secondary = Color(0xFF5E7E6E),
    onSecondary = Color.White,
    background = AppBackground,
    onBackground = AppText,
    surface = AppSurface,
    onSurface = AppText,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppTextSecondary,
    outline = AppOutline,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5CE38C),
    onPrimary = Color(0xFF062A16),
    primaryContainer = Color(0xFF11452A),
    onPrimaryContainer = Color(0xFFD6F7E1),
    secondary = Color(0xFF9FB7A8),
    onSecondary = Color(0xFF0D1812),
    background = Color(0xFF09100C),
    onBackground = Color(0xFFE5F2EA),
    surface = Color(0xFF111A14),
    onSurface = Color(0xFFE5F2EA),
    surfaceVariant = Color(0xFF16211A),
    onSurfaceVariant = Color(0xFF9DB0A4),
    outline = Color(0xFF284033),
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineSmall = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)

@Composable
fun StatusVaultTheme(
    content: @Composable () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = if (darkTheme) DarkColors.background.toArgb() else AppSurface.toArgb()
            window.navigationBarColor = if (darkTheme) DarkColors.surface.toArgb() else AppSurface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
