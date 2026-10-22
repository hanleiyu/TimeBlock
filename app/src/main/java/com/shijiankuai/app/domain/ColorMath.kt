package com.shijiankuai.app.domain

import kotlin.math.abs
import kotlin.math.roundToInt

object ColorMath {
    data class Hsv(val h: Float, val s: Float, val v: Float)

    fun hsvToArgb(h: Float, s: Float, v: Float): Long {
        val hh = ((h % 360f) + 360f) % 360f
        val sat = s.coerceIn(0f, 1f)
        val value = v.coerceIn(0f, 1f)
        val c = value * sat
        val x = c * (1f - abs((hh / 60f) % 2f - 1f))
        val m = value - c
        val (rp, gp, bp) = when {
            hh < 60f -> Triple(c, x, 0f)
            hh < 120f -> Triple(x, c, 0f)
            hh < 180f -> Triple(0f, c, x)
            hh < 240f -> Triple(0f, x, c)
            hh < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        fun ch(p: Float) = ((p + m) * 255f).roundToInt().coerceIn(0, 255)
        return argb(ch(rp), ch(gp), ch(bp))
    }

    fun normalize(color: Long): Long = 0xFF000000L or (color and 0xFFFFFFL)

    fun same(a: Long, b: Long): Boolean = normalize(a) == normalize(b)

    fun argbToHsv(color: Long): Hsv {
        val r = red(color) / 255f
        val g = green(color) / 255f
        val b = blue(color) / 255f
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val d = max - min
        val h = when {
            d == 0f -> 0f
            max == r -> 60f * (((g - b) / d) % 6f)
            max == g -> 60f * (((b - r) / d) + 2f)
            else -> 60f * (((r - g) / d) + 4f)
        }.let { if (it < 0f) it + 360f else it }
        val s = if (max == 0f) 0f else d / max
        return Hsv(h, s, max)
    }

    fun parseHex(raw: String): Long? {
        val s = raw.trim().removePrefix("#")
        val hex = when (s.length) {
            6 -> s
            8 -> s.takeLast(6)
            3 -> s.map { "$it$it" }.joinToString("")
            else -> return null
        }
        val n = hex.toLongOrNull(16) ?: return null
        return 0xFF000000L or n
    }

    fun toHex(color: Long): String = "#%06X".format(normalize(color) and 0xFFFFFF)

    fun red(color: Long): Int = ((normalize(color) shr 16) and 0xFF).toInt()
    fun green(color: Long): Int = ((normalize(color) shr 8) and 0xFF).toInt()
    fun blue(color: Long): Int = (normalize(color) and 0xFF).toInt()

    private fun argb(r: Int, g: Int, b: Int): Long =
        0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
}
