package com.shijiankuai.app.exercise.ui.week

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shijiankuai.app.exercise.domain.TargetMode
import com.shijiankuai.app.exercise.domain.toZhDate
import com.shijiankuai.app.exercise.ui.HomeViewModel
import com.shijiankuai.app.exercise.ui.components.AppCard
import com.shijiankuai.app.exercise.ui.components.LineChart
import com.shijiankuai.app.exercise.ui.components.ProgressBar
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.Aero
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.StrengthHue

@Composable
fun WeekScreen(
    vm: HomeViewModel,
    onOpenPlan: () -> Unit,
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        Text(ui.stageLine, color = Ink2, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))

        AppCard {
            ProgressBlock(
                title = if (ui.mode == TargetMode.PLAN) "有氧阶段目标" else "有氧目标",
                current = ui.aerobic,
                target = ui.aerobicTarget,
                unit = "分钟",
                color = Aero,
            )
            Spacer(Modifier.height(16.dp))
            ProgressBlock(
                title = "力量",
                current = ui.strength,
                target = ui.strengthTarget,
                unit = "分钟",
                color = StrengthHue,
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("平衡（可选）", color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(
                    if (ui.balanceGoal > 0) "${ui.balance}/${ui.balanceGoal}次" else "${ui.balance}次",
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        AppCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("本周总运动时长", color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("${ui.rawTotal}分钟", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "有氧 ${ui.rawAerobic} · 力量 ${ui.rawStrength} · 平衡 ${ui.rawBalance}",
                color = Ink2,
                fontSize = 12.sp,
            )
        }

        Spacer(Modifier.height(12.dp))
        AppCard {
            Text("每周总时长趋势（近8周）", color = Ink, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            val labels = if (ui.trend.isNotEmpty()) {
                listOf(ui.trend.first().weekStart.toZhDate(), "本周")
            } else emptyList()
            LineChart(
                values = ui.trend.map { it.rawTotal.toFloat() },
                labels = labels,
                color = Accent,
            )
        }

        Spacer(Modifier.height(12.dp))
        AppCard(onClick = onOpenPlan) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ui.planEntry, color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ChevronRight, null, tint = Ink2)
            }
        }
    }

    ui.advanceMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::dismissAdvance,
            confirmButton = { TextButton(onClick = vm::dismissAdvance) { Text("好的") } },
            title = { Text("阶段升级") },
            text = { Text(msg) },
        )
    }
}

@Composable
private fun ProgressBlock(
    title: String,
    current: Int,
    target: Int,
    unit: String,
    color: Color,
) {
    val remain = (target - current).coerceAtLeast(0)
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("$current/$target$unit", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        ProgressBar(if (target == 0) 0f else current.toFloat() / target, color)
        if (remain > 0) {
            Spacer(Modifier.height(8.dp))
            Text("还差 ${remain}$unit", color = Ink2, fontSize = 12.sp)
        }
    }
}
