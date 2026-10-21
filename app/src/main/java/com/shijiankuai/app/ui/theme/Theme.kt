package com.shijiankuai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = lightColorScheme(
    primary = Accent,
    onPrimary = Color(0xFFFFFFFF),
    secondary = Wake,
    onSecondary = Color(0xFFFFFFFF),
    background = Paper,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    surfaceVariant = PaperDeep,
    onSurfaceVariant = Ink2,
    outline = Line,
)

@Composable
fun TimeBlockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = appTypography(),
        content = content,
    )
}
