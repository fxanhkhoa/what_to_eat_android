package com.fxanhkhoa.what_to_eat_android.shared

enum class DifficultyLevel(val rawValue: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    val displayName: String
        get() = when (this) {
            EASY -> "Easy"
            MEDIUM -> "Medium"
            HARD -> "Hard"
        }

    val localizationKey: String
        get() = when (this) {
            EASY -> "difficulty_easy"
            MEDIUM -> "difficulty_medium"
            HARD -> "difficulty_hard"
        }

    val colorRes: Int
        get() = when (this) {
            EASY -> android.R.color.holo_green_light
            MEDIUM -> android.R.color.holo_orange_light
            HARD -> android.R.color.holo_red_light
        }

    val iconName: String
        get() = when (this) {
            EASY -> "easy"
            MEDIUM -> "medium"
            HARD -> "hard"
        }

    companion object {
        fun from(value: String?): DifficultyLevel {
            return values().find { it.rawValue.equals(value, ignoreCase = true) } ?: EASY
        }
    }
}
