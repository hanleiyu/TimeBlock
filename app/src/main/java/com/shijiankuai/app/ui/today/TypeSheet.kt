package com.shijiankuai.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.shijiankuai.app.domain.Presets
import com.shijiankuai.app.ui.TypeColorPicker
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.PaperDeep
import com.shijiankuai.app.ui.theme.Sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypePickerSheet(
    count: Int,
    types: List<BlockType>,
    onPick: (String) -> Unit,
    onClear: () -> Unit,
    onCancel: () -> Unit,
    onAddType: (String, Long) -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var adding by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = state,
        containerColor = Sheet,
    ) {
        if (adding) {
            AddTypeForm(
                onSave = { name, color ->
                    onAddType(name, color)
                    adding = false
                },
                onCancel = { adding = false },
            )
        } else {
            Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 28.dp)) {
                Text("标记这 $count 个时间块", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(Modifier.height(14.dp))
                types.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { t ->
                            TypeChip(t, Modifier.weight(1f)) { onPick(t.id) }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                        .clickable { adding = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+  添加类型", color = Ink2, fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaperDeep)
                            .clickable(onClick = onClear),
                        contentAlignment = Alignment.Center,
                    ) { Text("清除选区", color = Ink2) }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Ink)
                            .clickable(onClick = onCancel),
                        contentAlignment = Alignment.Center,
                    ) { Text("取消", color = Color(0xFFF7F1E8)) }
                }
            }
        }
    }
}

@Composable
private fun TypeChip(type: BlockType, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Paper)
            .border(1.dp, Line, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(Color(type.color.toInt())))
        Text(type.name, fontSize = 14.sp, color = Ink, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
fun AddTypeForm(onSave: (String, Long) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(Presets.swatches.first()) }
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(bottom = 28.dp),
    ) {
        Text("添加类型", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("名称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        TypeColorPicker(color = color, onColorChange = { color = it })
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaperDeep)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) { Text("取消", color = Ink2) }
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink)
                    .clickable(enabled = name.isNotBlank()) {
                        if (name.isNotBlank()) onSave(name.trim(), color)
                    },
                contentAlignment = Alignment.Center,
            ) { Text("保存类型", color = Color(0xFFF7F1E8)) }
        }
    }
}
