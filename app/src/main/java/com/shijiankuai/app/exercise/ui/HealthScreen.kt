package com.shijiankuai.app.exercise.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shijiankuai.app.exercise.data.ExerciseRepository
import com.shijiankuai.app.exercise.ui.month.MonthScreen
import com.shijiankuai.app.exercise.ui.week.WeekScreen

@Composable
fun HealthPane(
    repo: ExerciseRepository,
    week: Boolean,
    onOpenPlan: () -> Unit,
    onOpenWorkout: (Long) -> Unit,
    onOpenMetrics: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        if (week) {
            WeekScreen(
                vm = viewModel(factory = ExerciseVmFactory(repo)),
                onOpenPlan = onOpenPlan,
                onOpenMetrics = onOpenMetrics,
            )
        } else {
            MonthScreen(
                vm = viewModel(factory = ExerciseVmFactory(repo)),
                onOpen = onOpenWorkout,
                onOpenMetrics = onOpenMetrics,
            )
        }
    }
}
