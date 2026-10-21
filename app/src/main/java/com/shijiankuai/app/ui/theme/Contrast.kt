package com.shijiankuai.app.ui.theme

import kotlin.math.pow

object Contrast {
    fun ratio(fgRgb: Long, bgRgb: Long): Double {
        val a = luminance(fgRgb)
        val b = luminance(bgRgb)
        val lighter = maxOf(a, b)
        val darker = minOf(a, b)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun luminance(rgb: Long): Double {
        fun chan(v: Int): Double {
            val s = v / 255.0
            return if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        val r = ((rgb shr 16) and 0xFF).toInt()
        val g = ((rgb shr 8) and 0xFF).toInt()
        val b = (rgb and 0xFF).toInt()
        return 0.2126 * chan(r) + 0.7152 * chan(g) + 0.0722 * chan(b)
    }
}
