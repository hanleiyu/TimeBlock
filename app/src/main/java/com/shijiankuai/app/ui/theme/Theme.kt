package com.shijiankuai.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

private fun scheme(palette: Palette, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = palette.accent,
        onPrimary = Color(0xFF1C1A1F),
        secondary = Wake,
        onSecondary = Color(0xFF1C1A1F),
        background = palette.paper,
        onBackground = palette.ink,
        surface = palette.card,
        onSurface = palette.ink,
        surfaceVariant = palette.paperDeep,
        onSurfaceVariant = palette.ink2,
        outline = palette.line,
        primaryContainer = palette.highlightWash,
        onPrimaryContainer = palette.ink,
    )
} else {
    lightColorScheme(
        primary = palette.accent,
        onPrimary = Color(0xFFFFFFFF),
        secondary = Wake,
        onSecondary = Color(0xFFFFFFFF),
        background = palette.paper,
        onBackground = palette.ink,
        surface = palette.card,
        onSurface = palette.ink,
        surfaceVariant = palette.paperDeep,
        onSurfaceVariant = palette.ink2,
        outline = palette.line,
        primaryContainer = palette.highlightWash,
        onPrimaryContainer = palette.ink,
    )
}

@Composable
fun TimeBlockTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    val palette = if (dark) DarkPalette else LightPalette
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowInsetsControllerCompat(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme(palette, dark),
            typography = appTypography(),
            content = content,
        )
    }
}
