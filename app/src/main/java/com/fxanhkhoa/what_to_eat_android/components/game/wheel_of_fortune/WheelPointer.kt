package com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

@Composable
fun WheelPointer(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFF3A446)
) {
    Canvas(modifier = modifier.size(20.dp, 30.dp)) {
        val path = Path().apply {
            // Triangle pointing downward
            moveTo(size.width / 2f, size.height) // Bottom center
            lineTo(0f, 0f) // Top left
            lineTo(size.width, 0f) // Top right
            close()
        }

        drawPath(
            path = path,
            color = color,
            style = Fill
        )

        // Add shadow effect
        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.2f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    }
}

