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
    val selectedFill: Color,
    val onSelected: Color,
    val navSelected: Color,
    val navSelectedWash: Color,
    val daySelect: Color,
    val aero: Color,
    val strength: Color,
    val balance: Color,
    val periodMark: Color,
    val bowelMark: Color,
    val earlySleep: Color,
    val gold: Color,
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
    selectedFill = Color(0xFFFFFCF9),
    onSelected = Color(0xFF2B2E45),
    navSelected = Color(0xFF2B2E45),
    navSelectedWash = Color.Transparent,
    daySelect = Color(0xFF6B8F71),
    aero = Color(0xFF6B8F71),
    strength = Color(0xFF7A7EB0),
    balance = Color(0xFFD99A76),
    periodMark = Color(0xFFC45B5B),
    bowelMark = Color(0xFF6A8C9E),
    earlySleep = Color(0xFFE4C35A),
    gold = Color(0xFFE4C35A),
)

/** Dark = ExcerciseApp Theme.kt: true black + neon green, not deep paper. */
val DarkPalette = Palette(
    paper = Color(0xFF000000),
    paperDeep = Color(0xFF1C1C1F),
    card = Color(0xFF141414),
    ink = Color(0xFFF4F4F5),
    ink2 = Color(0xFFB4B4BB),
    ink3 = Color(0xFF8A8A93),
    line = Color(0xFF2A2A2E),
    accent = Color(0xFF00E676),
    sheet = Color(0xFF141414),
    unfilled = Color(0xFF2C2C31),
    tabBar = Color(0xFF000000),
    highlightWash = Color(0xFF2C2C31),
    highlightLine = Color(0xFF4A4A50),
    emptyPast = Color(0xFF1C1C1F),
    emptyFuture = Color(0xFF141414),
    pillWash = Color(0xFF2C2C31),
    pillWashText = Color(0xFFF4F4F5),
    pillDone = Color(0xFF12351F),
    pillDoneText = Color(0xFF4ADE80),
    pillPending = Color(0xFF222226),
    pillPendingText = Color(0xFFD4D4D8),
    onAccent = Color(0xFF000000),
    onInk = Color(0xFF000000),
    selectedFill = Color(0xFFE8E8ED),
    onSelected = Color(0xFF000000),
    navSelected = Color(0xFF000000),
    navSelectedWash = Color(0xFFE8E8ED),
    daySelect = Color(0xFF00E676),
    aero = Color(0xFF00E676),
    strength = Color(0xFF8B82F6),
    balance = Color(0xFFF5A524),
    periodMark = Color(0xFFFF4D6D),
    bowelMark = Color(0xFF5EB0FF),
    earlySleep = Color(0xFFFFDB58),
    gold = Color(0xFFFFC14A),
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
val SelectedFill: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.selectedFill
val OnSelected: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onSelected
val NavSelected: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.navSelected
val NavSelectedWash: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.navSelectedWash
val DaySelect: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.daySelect
val Aero: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.aero
val StrengthHue: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.strength
val BalanceHue: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.balance
val PeriodMark: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.periodMark
val BowelMark: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.bowelMark
val EarlySleepMark: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.earlySleep
val HealthGold: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.gold

val Wake = Color(0xFFD99A76)
val WakeDeep = Color(0xFFD99A76)
val Sleep = Color(0xFF5A5F92)
val Danger = Color(0xFFFF4D6D)
val GooseYellow = Color(0xFFFFDB58)
