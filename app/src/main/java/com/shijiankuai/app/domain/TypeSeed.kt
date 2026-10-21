package com.shijiankuai.app.domain

data class TypeSeedPlan(
    val insert: List<BlockType>,
    val sortUpdates: Map<String, Int>,
    val recolors: List<BlockType>,
)

object TypeSeed {
    fun plan(
        existing: List<BlockType>,
        catalog: List<BlockType> = Presets.types,
    ): TypeSeedPlan {
        val working = existing.toMutableList()
        val insert = mutableListOf<BlockType>()
        val sortUpdates = mutableMapOf<String, Int>()
        val recolors = mutableListOf<BlockType>()

        catalog.forEachIndexed { index, preset ->
            val matchIdx = working.indexOfFirst { it.id == preset.id }
            if (matchIdx >= 0) {
                val row = working[matchIdx]
                if (row.isPreset && row.color != preset.color) {
                    val next = row.copy(color = preset.color)
                    working[matchIdx] = next
                    recolors += next
                }
                return@forEachIndexed
            }
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

        return TypeSeedPlan(insert = insert, sortUpdates = sortUpdates, recolors = recolors)
    }
}
