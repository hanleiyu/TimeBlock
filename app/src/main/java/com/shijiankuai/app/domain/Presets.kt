package com.shijiankuai.app.domain

object Presets {
    val types: List<BlockType> = listOf(
        BlockType("work", "工作", 0xFF8EBFD2, 0, archived = false, isPreset = true, isSleep = false),
        BlockType("eat", "吃饭", 0xFFE5AD8C, 1, archived = false, isPreset = true, isSleep = false),
        BlockType(SLEEP_TYPE_ID, "睡觉", 0xFF808BD0, 2, archived = false, isPreset = true, isSleep = true),
        BlockType("study", "学习", 0xFFA996CF, 3, archived = false, isPreset = true, isSleep = false),
        BlockType("read", "阅读", 0xFFDE8B9D, 4, archived = false, isPreset = true, isSleep = false),
        BlockType("play", "玩", 0xFFE3C783, 5, archived = false, isPreset = true, isSleep = false),
        BlockType("social", "人际", 0xFF9CCB97, 6, archived = false, isPreset = true, isSleep = false),
        BlockType("sport", "运动", 0xFF74B3AE, 7, archived = false, isPreset = true, isSleep = false),
        BlockType("other", "其他", 0xFFA8A6A3, 8, archived = false, isPreset = true, isSleep = false),
    )

    val extraColors: List<Long> = listOf(
        0xFFC4B5E3,
        0xFFF2C4A8,
        0xFFB8D4C0,
        0xFFE8D4A0,
    )

    val swatches: List<Long> = types.map { it.color } + extraColors
}
