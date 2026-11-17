package com.shijiankuai.app.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.Line

/** Month-tab card/calendar chrome copied from 月·清单. */
object MonthCardSpec {
    const val radiusDp = 16
    const val borderWidthDp = 1
    const val calendarPadHDp = 10
    const val calendarPadVDp = 12
    const val sectionPadHDp = 16
    const val sectionPadVDp = 14
    const val gapDp = 12
    const val weekdaySp = 12
    const val dayNumberSp = 14
    const val dayCircleDp = 34
    const val selectedStrokeTenthsDp = 15
    const val todayStrokeDp = 1
    const val cellAspectTimes100 = 92
    const val countChipRadiusDp = 14
    const val countChipPadVDp = 6
    const val countChipGapDp = 8

    val cellAspect: Float get() = cellAspectTimes100 / 100f
    val selectedStrokeDp: Float get() = selectedStrokeTenthsDp / 10f
}

@Composable
fun MonthFrame(
    modifier: Modifier = Modifier,
    padHDp: Int = MonthCardSpec.sectionPadHDp,
    padVDp: Int = MonthCardSpec.sectionPadVDp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(MonthCardSpec.radiusDp.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Card)
            .border(MonthCardSpec.borderWidthDp.dp, Line, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = padHDp.dp, vertical = padVDp.dp),
        content = content,
    )
}
