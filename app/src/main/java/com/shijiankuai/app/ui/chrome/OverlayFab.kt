package com.shijiankuai.app.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.OnAccent

/** Shared overlay + control. Does not take column height. */
object OverlayFabSpec {
    const val consumesLayoutHeight = false
    const val sizeDp = 56
    const val endPaddingDp = 20
    const val bottomPaddingDp = 20
    const val minTouchTargetDp = 48
    const val scrollClearanceDp = 88
    const val healthRecordDescription = "＋ 记录"
    const val lifeEventDescription = "记录事件"
}

@Composable
fun BoxScope.OverlayFab(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .align(Alignment.BottomEnd)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.End))
            .padding(
                end = OverlayFabSpec.endPaddingDp.dp,
                bottom = OverlayFabSpec.bottomPaddingDp.dp,
            )
            .size(OverlayFabSpec.sizeDp.dp)
            .clip(CircleShape)
            .background(Accent)
            .semantics { this.contentDescription = contentDescription }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "+",
            color = OnAccent,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}
