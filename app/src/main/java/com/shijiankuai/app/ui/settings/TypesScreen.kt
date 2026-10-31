package com.shijiankuai.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.ui.TypeColorPicker
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.PaperDeep
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.today.AddTypeForm

@Composable
fun TypesScreen(
    types: List<BlockType>,
    onBack: () -> Unit,
    onUpdate: (BlockType) -> Unit,
    onReorder: (List<String>) -> Unit,
    onAdd: (String, Long) -> Unit,
) {
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BlockType?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹ 设置", color = Ink2, modifier = Modifier.clickable(onClick = onBack))
            Text("类型管理", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
            Text("完成", color = Accent, modifier = Modifier.clickable(onClick = onBack))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            types.forEachIndexed { i, t ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Card)
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                        .clickable { editing = t }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(16.dp).clip(CircleShape).background(Color(t.color.toInt())))
                    Text(t.name + if (t.archived) "（已归档）" else "", Modifier.weight(1f).padding(start = 10.dp), fontSize = 14.sp, color = Ink)
                    Text(if (t.isPreset) "预设" else "自定义", fontSize = 10.sp, color = Ink3, modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(6.dp)).background(PaperDeep).padding(horizontal = 6.dp, vertical = 2.dp))
                    Column {
                        Text("▲", fontSize = 10.sp, color = Ink3, modifier = Modifier.clickable(enabled = i > 0) {
                            if (i == 0) return@clickable
                            val ids = types.map { it.id }.toMutableList()
                            val tmp = ids[i]
                            ids[i] = ids[i - 1]
                            ids[i - 1] = tmp
                            onReorder(ids)
                        })
                        Text("▼", fontSize = 10.sp, color = Ink3, modifier = Modifier.clickable(enabled = i < types.lastIndex) {
                            if (i >= types.lastIndex) return@clickable
                            val ids = types.map { it.id }.toMutableList()
                            val tmp = ids[i]
                            ids[i] = ids[i + 1]
                            ids[i + 1] = tmp
                            onReorder(ids)
                        })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .clickable { adding = true }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) { Text("+  添加类型", color = Ink2) }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (adding) {
        AlertDialog(
            onDismissRequest = { adding = false },
            confirmButton = {},
            text = {
                AddTypeForm(
                    onSave = { n, c -> onAdd(n, c); adding = false },
                    onCancel = { adding = false },
                )
            },
        )
    }

    editing?.let { t ->
        EditTypeDialog(
            type = t,
            onDismiss = { editing = null },
            onSave = { onUpdate(it); editing = null },
        )
    }
}

@Composable
private fun EditTypeDialog(type: BlockType, onDismiss: () -> Unit, onSave: (BlockType) -> Unit) {
    var name by remember { mutableStateOf(type.name) }
    var color by remember { mutableStateOf(type.color) }
    var archived by remember { mutableStateOf(type.archived) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑类型") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                TypeColorPicker(color = color, onColorChange = { color = it })
                if (!type.isSleep) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (archived) "取消归档" else "归档（历史块保留）",
                        color = Ink2,
                        modifier = Modifier.clickable { archived = !archived },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(type.copy(name = name.trim().ifBlank { type.name }, color = color, archived = if (type.isSleep) false else archived))
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}
