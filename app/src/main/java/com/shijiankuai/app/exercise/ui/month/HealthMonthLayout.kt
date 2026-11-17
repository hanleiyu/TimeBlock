package com.shijiankuai.app.exercise.ui.month

import com.shijiankuai.app.domain.TodoMonth
import java.time.YearMonth

object HealthMonthLayout {
    fun grid(month: YearMonth) = TodoMonth.monthGrid(month)
}
