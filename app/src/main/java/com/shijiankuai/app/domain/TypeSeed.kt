package com.shijiankuai.app.domain

data class TypeSeedPlan(
    val insert: List<BlockType>,
    val sortUpdates: Map<String, Int>,
    val recolors: List<BlockType>,
    val retireIds: List<String> = emptyList(),
)

object TypeSeed {
    const val LEFTOVER_COMMUTE_ID = "commute"

    fun plan(
        existing: List<BlockType>,
        usedTypeIds: Set<String> = emptySet(),
        catalog: List<BlockType> = Presets.types,
    ): TypeSeedPlan {
        val working = existing.toMutableList()
        val insert = mutableListOf<BlockType>()
        val sortUpdates = mutableMapOf<String, Int>()
        val recolors = mutableListOf<BlockType>()
        val retireIds = mutableListOf<String>()

        val leftover = working.find { it.id == LEFTOVER_COMMUTE_ID }
        if (leftover != null && leftover.id !in usedTypeIds) {
            working.removeAll { it.id == LEFTOVER_COMMUTE_ID }
            retireIds += LEFTOVER_COMMUTE_ID
        }

        catalog.forEachIndexed { index, preset ->
            if (working.any { it.id == preset.id }) return@forEachIndexed
            if (working.any { it.name == preset.name }) return@forEachIndexed

            val prev = catalog.getOrNull(index - 1)
            val after = prev?.let { p -> working.find { it.id == p.id || it.name == p.name } }
            val sort = (after?.sortOrder ?: working.maxOfOrNull { it.sortOrder } ?: -1) + 1

            for (i in working.indices) {
                val row = working[i]
                if (row.sortOrder < sort) continue
                val shifted = row.copy(sortOrder = row.sortOrder + 1)
                working[i] = shifted
                val insertAt = insert.indexOfFirst { it.id == row.id }
                if (insertAt >= 0) {
                    insert[insertAt] = shifted
                } else {
                    sortUpdates[row.id] = shifted.sortOrder
                }
            }

            val placed = preset.copy(sortOrder = sort)
            insert += placed
            working += placed
        }

        return TypeSeedPlan(
            insert = insert,
            sortUpdates = sortUpdates,
            recolors = recolors,
            retireIds = retireIds,
        )
    }
}
