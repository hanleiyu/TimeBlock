package com.shijiankuai.app.exercise.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shijiankuai.app.exercise.domain.Stage
import com.shijiankuai.app.exercise.domain.TargetMode
import com.shijiankuai.app.exercise.ui.PlanViewModel
import com.shijiankuai.app.exercise.ui.components.AppCard
import com.shijiankuai.app.exercise.ui.components.ScreenHeader
import com.shijiankuai.app.exercise.ui.components.SoftHintCard
import com.shijiankuai.app.exercise.ui.components.StatusPill
import com.shijiankuai.app.ui.theme.Aero
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.PaperDeep
import com.shijiankuai.app.ui.theme.PillDone
import com.shijiankuai.app.ui.theme.PillDoneText
import com.shijiankuai.app.ui.theme.PillPending
import com.shijiankuai.app.ui.theme.PillPendingText
import com.shijiankuai.app.ui.theme.PillWash
import com.shijiankuai.app.ui.theme.PillWashText
import com.shijiankuai.app.ui.theme.StrengthHue

@Composable
fun PlanScreen(vm: PlanViewModel, onBack: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 28.dp),
    ) {
        ScreenHeader(title = "训练计划", onBack = onBack)
        Spacer(Modifier.height(14.dp))

        AppCard {
            Text(ui.stageLine, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "本阶段目标：有氧 ${ui.aerobicTarget} · 力量 ${ui.strengthTarget} 分钟",
                color = Ink2,
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.height(12.dp))
        AppCard {
            Text("阶段时间线", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Stage.entries.forEachIndexed { index, stage ->
                val spent = ui.weeksSpent[stage] ?: 0
                val status = when {
                    ui.mode == TargetMode.FIXED -> StageStatus.PENDING
                    stage == ui.stage -> StageStatus.CURRENT
                    stageOrdinal(stage) < stageOrdinal(ui.stage) -> StageStatus.DONE
                    else -> StageStatus.PENDING
                }
                StageRow(
                    stage = stage,
                    weeks = spent,
                    status = status,
                    aerobic = ui.stageTargets.aerobic(stage),
                    strength = ui.stageTargets.strength(stage),
                )
                if (index != Stage.entries.lastIndex) {
                    Box(
                        Modifier
                            .padding(start = 7.dp, top = 2.dp, bottom = 2.dp)
                            .size(width = 2.dp, height = 16.dp)
                            .background(PaperDeep),
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        AppCard {
            Text("升级条件", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "连续 ${ui.weeksToAdvance} 周同时达成有氧和力量目标 → 自动进入下一阶段；未达标则连续达标清零，停留在当前阶段。平衡次数不计入是否达标。只评估已结束的周。",
                color = Ink2,
                fontSize = 13.sp,
                lineHeight = 20.sp,
            )
        }

        if (ui.upgradeHint != null) {
            Spacer(Modifier.height(12.dp))
            SoftHintCard(ui.upgradeHint!!)
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

private enum class StageStatus { DONE, CURRENT, PENDING }

private fun stageOrdinal(stage: Stage) = Stage.entries.indexOf(stage)

@Composable
private fun StageRow(stage: Stage, weeks: Int, status: StageStatus, aerobic: Int, strength: Int) {
    val (dot, pillBg, pillFg, pillText) = when (status) {
        StageStatus.DONE -> Quad(Aero, PillDone, PillDoneText, "已完成")
        StageStatus.CURRENT -> Quad(StrengthHue, PillWash, PillWashText, "进行中")
        StageStatus.PENDING -> Quad(Ink3, PillPending, PillPendingText, "未开始")
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 4.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(dot),
        )
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val weekLabel = if (weeks > 0 || status == StageStatus.CURRENT) " · ${weeks} 周" else ""
                Text(
                    "${stage.label}$weekLabel",
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                StatusPill(pillText, pillBg, pillFg)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "有氧 $aerobic 分钟/周 · 力量 $strength 分钟/周",
                color = Ink2,
                fontSize = 12.sp,
            )
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
