@file:OptIn(ExperimentalTextApi::class)

package com.shijiankuai.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.shijiankuai.app.R

private fun noto(weight: FontWeight, axis: Int) = Font(
    resId = R.font.noto_sans_sc,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axis)),
)

val NotoSansSc = FontFamily(
    noto(FontWeight.Normal, 400),
    noto(FontWeight.Medium, 500),
    noto(FontWeight.SemiBold, 600),
    noto(FontWeight.Bold, 700),
)

fun appTypography(): Typography {
    val t = Typography()
    return Typography(
        displayLarge = t.displayLarge.readable(),
        displayMedium = t.displayMedium.readable(),
        displaySmall = t.displaySmall.readable(),
        headlineLarge = t.headlineLarge.readable(),
        headlineMedium = t.headlineMedium.readable(),
        headlineSmall = t.headlineSmall.readable(),
        titleLarge = t.titleLarge.readable(),
        titleMedium = t.titleMedium.readable(),
        titleSmall = t.titleSmall.readable(),
        bodyLarge = t.bodyLarge.readable(),
        bodyMedium = t.bodyMedium.readable(),
        bodySmall = t.bodySmall.readable(),
        labelLarge = t.labelLarge.readable(),
        labelMedium = t.labelMedium.readable(),
        labelSmall = t.labelSmall.readable(),
    )
}

private fun TextStyle.readable(): TextStyle = copy(
    fontFamily = NotoSansSc,
    fontWeight = bumped(fontWeight),
)

internal fun bumped(weight: FontWeight?): FontWeight {
    val w = weight?.weight ?: 400
    return when {
        w <= 450 -> FontWeight.Medium
        w <= 550 -> FontWeight.SemiBold
        w <= 650 -> FontWeight.Bold
        else -> weight ?: FontWeight.Bold
    }
}
