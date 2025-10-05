package com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune

import androidx.compose.ui.graphics.Color

object WheelColorPalette {
    val palette = listOf(
        Color(0xFF311300), // Dark brown
        Color(0xFF502400), // Brown
        Color(0xFF5F2E04), // Brown
        Color(0xFF6D390F), // Brown
        Color(0xFF7B441A), // Brown
        Color(0xFF8A5025), // Brown
        Color(0xFFA7683A), // Light brown
        Color(0xFFC58151), // Tan
        Color(0xFFE39B69), // Light tan
        Color(0xFFFFB786), // Peach
        Color(0xFFFFDCC6), // Light peach
        Color(0xFFFFEDE4), // Very light peach
        Color(0xFFFFF8F5), // Almost white
        Color(0xFFFFFBFF)  // White
    )

    val textColors = listOf(
        Color.White,      // For dark brown
        Color.White,      // For brown
        Color.White,      // For brown
        Color.White,      // For brown
        Color.White,      // For brown
        Color.White,      // For brown
        Color.White,      // For light brown
        Color(0xFFF3A446), // Primary color for tan
        Color(0xFFF3A446), // Primary color for light tan
        Color(0xFFF3A446), // Primary color for peach
        Color(0xFFF3A446), // Primary color for light peach
        Color(0xFFF3A446), // Primary color for very light peach
        Color(0xFFF3A446), // Primary color for almost white
        Color(0xFFF3A446)  // Primary color for white
    )

    fun getColorForIndex(index: Int, reverse: Boolean = false): Color {
        return if (reverse) {
            palette[(palette.size - 1) - (index % palette.size)]
        } else {
            palette[index % palette.size]
        }
    }

    fun getTextColorForIndex(index: Int, reverse: Boolean = false): Color {
        return if (reverse) {
            textColors[(textColors.size - 1) - (index % textColors.size)]
        } else {
            textColors[index % textColors.size]
        }
    }
}
