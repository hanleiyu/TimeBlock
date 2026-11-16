package com.shijiankuai.app.ui.chrome

import com.shijiankuai.app.domain.PeriodKind
import java.time.LocalDate
import java.time.YearMonth

/** Shared previous/next date-bar contract. Chevrons stay uncircled. */
object DateNavSpec {
    const val enclosesChevrons = false
    const val labelCentered = true
    const val chevronsAtOuterEnds = true
    const val labelSizeSp = 16
    const val chevronSizeDp = 22
    const val minTouchTargetDp = 48
    const val verticalPaddingDp = 8
    const val prevContentDescription = "上一"
    const val nextContentDescription = "下一"

    fun checklistLabel(kind: PeriodKind, start: LocalDate, end: LocalDate): String =
        kind.barLabel(start, end)

    fun monthHealthLabel(month: YearMonth): String =
        PeriodKind.MONTH.barLabel(month.atDay(1), month.atEndOfMonth())
}
