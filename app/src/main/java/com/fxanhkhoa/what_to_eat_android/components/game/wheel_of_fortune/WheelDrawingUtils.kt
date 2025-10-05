package com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

object WheelDrawingUtils {
    fun createWheelSectionPath(
        centerX: Float,
        centerY: Float,
        radius: Float,
        startAngle: Float,
        sweepAngle: Float
    ): Path {
        return Path().apply {
            moveTo(centerX, centerY)

            // Draw arc
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = centerX - radius,
                    top = centerY - radius,
                    right = centerX + radius,
                    bottom = centerY + radius
                ),
                startAngleDegrees = startAngle,
                sweepAngleDegrees = sweepAngle,
                forceMoveTo = false
            )

            lineTo(centerX, centerY)
            close()
        }
    }

    fun calculateTextPosition(
        centerX: Float,
        centerY: Float,
        radius: Float,
        angle: Float
    ): Offset {
        val angleRad = angle * PI.toFloat() / 180f
        val textRadius = radius * 0.65f // Position text at 65% of radius

        return Offset(
            x = centerX + textRadius * cos(angleRad),
            y = centerY + textRadius * sin(angleRad)
        )
    }

    fun truncateDishName(name: String, maxChars: Int): String {
        return if (name.length > maxChars) {
            name.take(maxChars - 1) + "…"
        } else {
            name
        }
    }

    fun getMaxCharsForSections(totalSections: Int): Int {
        return when (totalSections) {
            in 1..4 -> 20
            in 5..6 -> 15
            7, 8 -> 12
            9, 10 -> 10
            else -> 8
        }
    }
}

