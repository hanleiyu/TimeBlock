package com.shijiankuai.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.BuildConfig
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.PaperDeep
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    types: List<BlockType>,
    onOpenTypes: () -> Unit,
    exportBackup: suspend () -> String,
    restore: suspend (String) -> Boolean,
    showAllTime: Boolean = true,
    onShowAllTime: (Boolean) -> Unit = {},
    autoBackup: Boolean = true,
    lastBackupLabel: String = "尚未备份",
    onAutoBackup: (Boolean) -> Unit = {},
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val pending = remember { mutableStateOf<String?>(null) }
    var confirmRestore by remember { mutableStateOf<String?>(null) }
    var banner by remember { mutableStateOf<String?>(null) }
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        val body = pending.value ?: return@rememberLauncherForActivityResult
        pending.value = null
        uri ?: return@rememberLauncherForActivityResult
        ctx.contentResolver.openOutputStream(uri)?.use { it.write(body.toByteArray()) }
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val text = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return@rememberLauncherForActivityResult
        confirmRestore = text
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text("设置", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        if (banner != null) {
            Text(
                banner!!,
                color = Ink2,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PaperDeep)
                    .clickable { banner = null }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(14.dp))
        Group("时间块") {
            RowItem(title = "时间块长度", value = "10 分钟")
        }
        Spacer(Modifier.height(12.dp))
        Group("统计") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onShowAllTime(!showAllTime) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("显示所有时间", fontSize = 14.sp, color = Ink, modifier = Modifier.weight(1f))
                Switch(
                    checked = showAllTime,
                    onCheckedChange = onShowAllTime,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Accent,
                    ),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Group("类型") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenTypes)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("类型管理", fontSize = 14.sp, color = Ink)
                    Text("重命名、换色、排序、归档", fontSize = 11.sp, color = Ink3)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    types.filter { !it.archived }.take(5).forEach {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(Color(it.color.toInt())))
                    }
                }
                Text("›", color = Ink2, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Group("数据 · 仅本机") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onAutoBackup(!autoBackup) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("每晚 0 点自动备份到下载目录", fontSize = 14.sp, color = Ink)
                    Text(lastBackupLabel, fontSize = 11.sp, color = Ink3)
                }
                Switch(
                    checked = autoBackup,
                    onCheckedChange = onAutoBackup,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Accent,
                    ),
                )
            }
            RowItem("本地备份") {
                scope.launch {
                    pending.value = exportBackup()
                    create.launch("时间块-备份.txt")
                }
            }
            RowItem("从备份恢复") {
                open.launch(arrayOf("application/json", "text/plain", "*/*"))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "离线使用 · 无账号 · 无网络",
            fontSize = 11.sp,
            color = Ink3,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "版本 ${BuildConfig.VERSION_NAME}",
            fontSize = 11.sp,
            color = Ink3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
        )
    }

    confirmRestore?.let { body ->
        AlertDialog(
            onDismissRequest = { confirmRestore = null },
            title = { Text("从备份恢复？") },
            text = { Text("会覆盖当前本机的类型、时间块和睡觉记录，且不能撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    val json = body
                    confirmRestore = null
                    scope.launch {
                        banner = if (restore(json)) "已从备份恢复" else "备份文件读不出来"
                    }
                }) { Text("覆盖恢复") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun Group(cap: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
    ) {
        Text(cap, fontSize = 11.sp, color = Ink3, modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 2.dp))
        content()
    }
}

@Composable
private fun RowItem(title: String, sub: String? = null, value: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = Ink)
            if (sub != null) Text(sub, fontSize = 11.sp, color = Ink3)
        }
        if (value != null) Text(value, fontSize = 13.sp, color = Ink2)
        if (onClick != null) Text("›", color = Ink2)
    }
}
