package com.shijiankuai.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.domain.ColorMath
import com.shijiankuai.app.domain.Presets
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Line

@Composable
fun TypeColorPicker(
    color: Long,
    onColorChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initial = ColorMath.argbToHsv(color)
    var hue by remember { mutableFloatStateOf(initial.h) }
    var sat by remember { mutableFloatStateOf(initial.s) }
    var value by remember { mutableFloatStateOf(initial.v) }
    var hexText by remember { mutableStateOf(ColorMath.toHex(color)) }
    var lastEmitted by remember { mutableLongStateOf(ColorMath.normalize(color)) }
    val hueNow = rememberUpdatedState(hue)
    val satNow = rememberUpdatedState(sat)
    val valueNow = rememberUpdatedState(value)

    LaunchedEffect(color) {
        val n = ColorMath.normalize(color)
        if (n == lastEmitted) return@LaunchedEffect
        val next = ColorMath.argbToHsv(n)
        hue = next.h
        sat = next.s
        value = next.v
        hexText = ColorMath.toHex(n)
        lastEmitted = n
    }

    fun emit(h: Float, s: Float, v: Float) {
        hue = h
        sat = s
        value = v
        val argb = ColorMath.hsvToArgb(h, s, v)
        lastEmitted = argb
        hexText = ColorMath.toHex(argb)
        onColorChange(argb)
    }

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(width = 44.dp, height = 28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(ColorMath.normalize(color).toInt()))
                    .border(1.dp, Line, RoundedCornerShape(8.dp)),
            )
            OutlinedTextField(
                value = hexText,
                onValueChange = { raw ->
                    hexText = raw.uppercase().take(9)
                    ColorMath.parseHex(raw)?.let { parsed ->
                        val n = ColorMath.normalize(parsed)
                        lastEmitted = n
                        val next = ColorMath.argbToHsv(n)
                        hue = next.h
                        sat = next.s
                        value = next.v
                        onColorChange(n)
                    }
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                ),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        Presets.swatches.chunked(8).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                row.forEach { c ->
                    val on = ColorMath.same(c, color)
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(ColorMath.normalize(c).toInt()))
                            .then(
                                if (on) Modifier.border(2.dp, Ink, CircleShape)
                                else Modifier.border(1.dp, Ink.copy(alpha = 0.2f), CircleShape),
                            )
                            .clickable {
                                val n = ColorMath.normalize(c)
                                lastEmitted = n
                                val next = ColorMath.argbToHsv(n)
                                hue = next.h
                                sat = next.s
                                value = next.v
                                hexText = ColorMath.toHex(n)
                                onColorChange(n)
                            },
                    )
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .pickColor { x, y -> emit(hueNow.value, x, 1f - y) },
        ) {
            val hueColor = Color(ColorMath.hsvToArgb(hue, 1f, 1f).toInt())
            val ink = Ink
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                val thumb = Offset(sat * size.width, (1f - value) * size.height)
                drawCircle(Color.White, 7.dp.toPx(), thumb)
                drawCircle(ink, 7.dp.toPx(), thumb, style = Stroke(1.6.dp.toPx()))
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(999.dp))
                .pickColor { x, _ -> emit(x * 360f, satNow.value, valueNow.value) },
        ) {
            val hues = listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map {
                Color(ColorMath.hsvToArgb(it, 1f, 1f).toInt())
            }
            val ink = Ink
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.horizontalGradient(hues))
                val thumb = Offset((hue / 360f) * size.width, size.height / 2f)
                drawCircle(Color.White, 7.dp.toPx(), thumb)
                drawCircle(ink, 7.dp.toPx(), thumb, style = Stroke(1.6.dp.toPx()))
            }
        }
    }
}

private fun Modifier.pickColor(onRatio: (x: Float, y: Float) -> Unit): Modifier = composed {
    val latest = rememberUpdatedState(onRatio)
    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown()
            fun emit(p: Offset) {
                val x = (p.x / size.width.toFloat()).coerceIn(0f, 1f)
                val y = (p.y / size.height.toFloat()).coerceIn(0f, 1f)
                latest.value(x, y)
            }
            emit(down.position)
            drag(down.id) { change ->
                emit(change.position)
                change.consume()
            }
        }
    }
}
