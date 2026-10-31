package com.shijiankuai.app.exercise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shijiankuai.app.exercise.data.ExerciseRepository
import com.shijiankuai.app.exercise.ui.components.Segmented
import com.shijiankuai.app.exercise.ui.metrics.MetricsScreen
import com.shijiankuai.app.exercise.ui.month.MonthScreen
import com.shijiankuai.app.exercise.ui.week.WeekScreen
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Paper
import java.time.LocalDate

enum class HealthPage(val label: String) {
    WEEK("周"),
    MONTH("月"),
    METRICS("指标"),
}

@Composable
fun HealthScreen(
    repo: ExerciseRepository,
    page: HealthPage,
    onPage: (HealthPage) -> Unit,
    onOpenPlan: () -> Unit,
    onOpenWorkout: (Long) -> Unit,
    onAddWorkout: (LocalDate) -> Unit,
    onOpenMetric: (Long) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text("健康", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(12.dp))
        Segmented(
            options = HealthPage.entries.map { it.label },
            selected = page.label,
            onSelect = { label -> onPage(HealthPage.entries.first { it.label == label }) },
        )
        Spacer(Modifier.height(12.dp))
        when (page) {
            HealthPage.WEEK -> WeekScreen(
                vm = viewModel(factory = ExerciseVmFactory(repo)),
                onOpenPlan = onOpenPlan,
            )
            HealthPage.MONTH -> MonthScreen(
                vm = viewModel(factory = ExerciseVmFactory(repo)),
                onOpen = onOpenWorkout,
                onAdd = onAddWorkout,
            )
            HealthPage.METRICS -> MetricsScreen(
                vm = viewModel(factory = ExerciseVmFactory(repo)),
                onOpen = onOpenMetric,
            )
        }
    }
}
