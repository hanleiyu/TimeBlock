package com.shijiankuai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.shijiankuai.app.R

val NotoSansSc = FontFamily(
    Font(R.font.noto_sans_sc, FontWeight.Normal),
    Font(R.font.noto_sans_sc, FontWeight.Medium),
    Font(R.font.noto_sans_sc, FontWeight.SemiBold),
)

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
        typography = MaterialTheme.typography.copy(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = NotoSansSc),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = NotoSansSc),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = NotoSansSc),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = NotoSansSc),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = NotoSansSc),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = NotoSansSc),
            bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSansSc),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = NotoSansSc),
            labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = NotoSansSc),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = NotoSansSc),
        ),
        content = content,
    )
}
