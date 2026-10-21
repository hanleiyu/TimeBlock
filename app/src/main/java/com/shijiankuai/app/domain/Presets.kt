package com.shijiankuai.app.domain

object Presets {
    val types: List<BlockType> = listOf(
        BlockType("work", "工作", 0xFF7A8B99, 0, archived = false, isPreset = true, isSleep = false),
        BlockType("eat", "吃饭", 0xFFC9A07C, 1, archived = false, isPreset = true, isSleep = false),
        BlockType(SLEEP_TYPE_ID, "睡觉", 0xFF545D72, 2, archived = false, isPreset = true, isSleep = true),
        BlockType("study", "学习", 0xFF8E7F9A, 3, archived = false, isPreset = true, isSleep = false),
        BlockType("read", "阅读", 0xFFB88888, 4, archived = false, isPreset = true, isSleep = false),
        BlockType("play", "玩", 0xFFD0B07A, 5, archived = false, isPreset = true, isSleep = false),
        BlockType("social", "人际", 0xFF7FA0A0, 6, archived = false, isPreset = true, isSleep = false),
        BlockType("sport", "运动", 0xFFC48A6E, 7, archived = false, isPreset = true, isSleep = false),
    )

    val extraColors: List<Long> = listOf(
        0xFF8A9B7A,
        0xFF6F8F8A,
        0xFFA67C7C,
        0xFF8C7B6B,
        0xFF7B8C9E,
        0xFFB39B7A,
    )
}
