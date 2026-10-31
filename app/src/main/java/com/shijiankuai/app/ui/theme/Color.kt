package com.shijiankuai.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class Palette(
    val paper: Color,
    val paperDeep: Color,
    val card: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val line: Color,
    val accent: Color,
    val sheet: Color,
    val unfilled: Color,
    val tabBar: Color,
    val highlightWash: Color,
    val highlightLine: Color,
    val emptyPast: Color,
    val emptyFuture: Color,
    val pillWash: Color,
    val pillWashText: Color,
    val pillDone: Color,
    val pillDoneText: Color,
    val pillPending: Color,
    val pillPendingText: Color,
    val onAccent: Color,
    val onInk: Color,
)

val LightPalette = Palette(
    paper = Color(0xFFFBF5F1),
    paperDeep = Color(0xFFF3EBE5),
    card = Color(0xFFFFFCF9),
    ink = Color(0xFF2B2E45),
    ink2 = Color(0xFF5C5F75),
    ink3 = Color(0xFF5E6178),
    line = Color(0xFFE8E0D8),
    accent = Color(0xFF5A5F92),
    sheet = Color(0xFFFFFCF9),
    unfilled = Color(0xFFEDE6E0),
    tabBar = Color(0xFFFBF5F1),
    highlightWash = Color(0xFFE8E9F4),
    highlightLine = Color(0xFFD4D6E8),
    emptyPast = Color(0xFFEDE6E0),
    emptyFuture = Color(0xFFF6F1EC),
    pillWash = Color(0xFFE8E9F4),
    pillWashText = Color(0xFF5A5F92),
    pillDone = Color(0xFFE4F2F0),
    pillDoneText = Color(0xFF4A7A58),
    pillPending = Color(0xFFF3EBE5),
    pillPendingText = Color(0xFF5C5F75),
    onAccent = Color(0xFFFFFFFF),
    onInk = Color(0xFFF7F1E8),
)

val DarkPalette = Palette(
    paper = Color(0xFF1C1A1F),
    paperDeep = Color(0xFF26232B),
    card = Color(0xFF2C2933),
    ink = Color(0xFFF4EFE8),
    ink2 = Color(0xFFC4BEC8),
    ink3 = Color(0xFF9E98A6),
    line = Color(0xFF3E3A46),
    accent = Color(0xFF9AA0C8),
    sheet = Color(0xFF2C2933),
    unfilled = Color(0xFF3A3642),
    tabBar = Color(0xFF1C1A1F),
    highlightWash = Color(0xFF35344A),
    highlightLine = Color(0xFF4A4860),
    emptyPast = Color(0xFF35323C),
    emptyFuture = Color(0xFF2A2730),
    pillWash = Color(0xFF35344A),
    pillWashText = Color(0xFFC4C8E8),
    pillDone = Color(0xFF2A3D32),
    pillDoneText = Color(0xFF8FCB9A),
    pillPending = Color(0xFF35323C),
    pillPendingText = Color(0xFFB8B3C0),
    onAccent = Color(0xFF1C1A1F),
    onInk = Color(0xFF1C1A1F),
)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

val Paper: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.paper
val PaperDeep: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.paperDeep
val Card: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.card
val Ink: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.ink
val Ink2: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.ink2
val Ink3: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.ink3
val Line: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.line
val Accent: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.accent
val Sheet: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.sheet
val Unfilled: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.unfilled
val TabBar: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.tabBar
val HighlightWash: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.highlightWash
val HighlightLine: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.highlightLine
val EmptyPast: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.emptyPast
val EmptyFuture: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.emptyFuture
val PillWash: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.pillWash
val PillWashText: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.pillWashText
val PillDone: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.pillDone
val PillDoneText: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.pillDoneText
val PillPending: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.pillPending
val PillPendingText: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.pillPendingText
val OnAccent: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onAccent
val OnInk: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onInk

val Wake = Color(0xFFD99A76)
val WakeDeep = Color(0xFFD99A76)
val Sleep = Color(0xFF5A5F92)

/** Health category accents — muted so they sit on paper and stay readable on dark. */
val Aero = Color(0xFF6B8F71)
val StrengthHue = Color(0xFF7A7EB0)
val BalanceHue = Color(0xFFD99A76)
val PeriodMark = Color(0xFFC45B5B)
val BowelMark = Color(0xFF6A8C9E)
val EarlySleepMark = Color(0xFFE4C35A)
val HealthGold = Color(0xFFE4C35A)
