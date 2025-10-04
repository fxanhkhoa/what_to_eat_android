package com.fxanhkhoa.what_to_eat_android.components.game.WheelOfFortune

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language

@Composable
fun WheelCanvas(
    dishes: List<DishModel>,
    rotationAngle: Float,
    language: Language,
    onSectionTap: (DishModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .size(300.dp)
            .pointerInput(dishes) {
                detectTapGestures { offset ->
                    // Calculate which section was tapped
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY

                    // Calculate angle from center
                    var angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (angle < 0) angle += 360f

                    // Adjust for rotation
                    angle = (angle - rotationAngle) % 360f
                    if (angle < 0) angle += 360f

                    // Calculate which section
                    val sectionAngle = 360f / dishes.size
                    val sectionIndex = (angle / sectionAngle).toInt() % dishes.size

                    if (sectionIndex in dishes.indices) {
                        onSectionTap(dishes[sectionIndex])
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f
        val radius = minOf(canvasWidth, canvasHeight) / 2f

        if (dishes.isEmpty()) return@Canvas

        val sectionAngle = 360f / dishes.size

        rotate(degrees = rotationAngle, pivot = Offset(centerX, centerY)) {
            dishes.forEachIndexed { index, dish ->
                val startAngle = sectionAngle * index
                val middleAngle = startAngle + sectionAngle / 2f

                // Draw section
                val sectionPath = WheelDrawingUtils.createWheelSectionPath(
                    centerX = centerX,
                    centerY = centerY,
                    radius = radius,
                    startAngle = startAngle,
                    sweepAngle = sectionAngle
                )

                drawPath(
                    path = sectionPath,
                    color = WheelColorPalette.getColorForIndex(index)
                )

                // Draw border
                drawPath(
                    path = sectionPath,
                    color = Color.White,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Draw text
                val dishName = dish.getTitle(language.code) ?: dish.slug
                val maxChars = WheelDrawingUtils.getMaxCharsForSections(dishes.size)
                val truncatedName = WheelDrawingUtils.truncateDishName(dishName, maxChars)

                val textLayoutResult = textMeasurer.measure(
                    text = AnnotatedString(truncatedName),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = WheelColorPalette.getTextColorForIndex(index)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val textPosition = WheelDrawingUtils.calculateTextPosition(
                    centerX = centerX,
                    centerY = centerY,
                    radius = radius,
                    angle = middleAngle
                )

                rotate(
                    degrees = middleAngle,
                    pivot = textPosition
                ) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = textPosition.x - textLayoutResult.size.width / 2f,
                            y = textPosition.y - textLayoutResult.size.height / 2f - 10.dp.toPx()
                        )
                    )
                }
            }
        }

        // Draw center circle (not rotating)
        drawCircle(
            color = Color.White,
            radius = 30.dp.toPx(),
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFF3A446), Color(0xFFA06235))
            ),
            radius = 28.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}
