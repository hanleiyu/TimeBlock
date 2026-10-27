package com.shijiankuai.app.ui.todo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.shijiankuai.app.domain.TodoCategory

val DoneGreen = Color(0xFF5B9A72)

fun categoryIcon(category: TodoCategory): ImageVector = when (category) {
    TodoCategory.WORK -> Icons.Outlined.WorkOutline
    TodoCategory.STUDY -> Icons.AutoMirrored.Outlined.MenuBook
    TodoCategory.HEALTH -> Icons.Outlined.Eco
    TodoCategory.LIFE -> Icons.Outlined.Home
}

fun categoryTint(category: TodoCategory): Color = when (category) {
    TodoCategory.WORK -> Color(0xFF8EBFD2)
    TodoCategory.HEALTH -> Color(0xFF74B3AE)
    TodoCategory.LIFE -> Color(0xFFE5AD8C)
    TodoCategory.STUDY -> Color(0xFFA996CF)
}

fun categoryWash(category: TodoCategory): Color = when (category) {
    TodoCategory.WORK -> Color(0xFFE8E9F4)
    TodoCategory.HEALTH -> Color(0xFFE4F2F0)
    TodoCategory.LIFE -> Color(0xFFF8EEE7)
    TodoCategory.STUDY -> Color(0xFFECEEF8)
}
