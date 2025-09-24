package com.fxanhkhoa.what_to_eat_android.shared.contant

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Reusable gradient colors and brushes used across the app.
 */

// Gradient color pair used for dish titles (hex: #F3A446 -> #A06235)
val TitleGradientColors: List<Color> = listOf(
    Color(0xFFF3A446),
    Color(0xFFA06235)
)

// Convenience horizontal Brush using the same colors
val DishTitleHorizontalGradient: Brush = Brush.horizontalGradient(TitleGradientColors)
