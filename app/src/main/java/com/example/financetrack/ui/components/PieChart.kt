package com.example.financetrack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.financetrack.ui.theme.CardBackground
import com.example.financetrack.ui.theme.TextPrimary
import kotlin.math.atan2
import kotlin.math.hypot

/** Una rebanada del gráfico circular. */
data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color,
)

/**
 * Gráfico circular (dona) 100% dibujado con Compose.
 * Reemplaza a MPAndroidChart sin dependencias externas.
 */
@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    holeColor: Color = CardBackground,
    holeRadiusFraction: Float = 0.62f,
    centerText: String? = null,
    selectedIndex: Int? = null,
    onSliceClick: ((Int) -> Unit)? = null,
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (total <= 0f) return@Box

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(slices, total) {
                    detectTapGestures { offset ->
                        if (onSliceClick == null) return@detectTapGestures
                        val diameter = minOf(size.width, size.height).toFloat()
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val dist = hypot(dx, dy)
                        val outer = diameter / 2f
                        val inner = outer * holeRadiusFraction
                        if (dist in inner..outer) {
                            // Ángulo medido desde las 12 en punto, en sentido horario.
                            var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            deg = (deg + 90f + 360f) % 360f
                            var acc = 0f
                            slices.forEachIndexed { index, slice ->
                                val sweep = slice.value / total * 360f
                                if (deg >= acc && deg < acc + sweep) {
                                    onSliceClick(index)
                                    return@detectTapGestures
                                }
                                acc += sweep
                            }
                        }
                    }
                }
        ) {
            val diameter = size.minDimension
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f,
            )
            val arcSize = Size(diameter, diameter)
            val gap = if (slices.size > 1) 2f else 0f

            var startAngle = -90f
            slices.forEachIndexed { index, slice ->
                val sweep = slice.value / total * 360f
                val color = if (selectedIndex == index) {
                    slice.color.copy(alpha = 0.75f)
                } else {
                    slice.color
                }
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = (sweep - gap).coerceAtLeast(0f),
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize,
                )
                startAngle += sweep
            }

            drawCircle(
                color = holeColor,
                radius = diameter * holeRadiusFraction / 2f,
                center = center,
            )
        }

        if (centerText != null) {
            Text(
                text = centerText,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
