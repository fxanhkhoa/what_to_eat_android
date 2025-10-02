package com.fxanhkhoa.what_to_eat_android.shared

enum class DifficultyLevel(val rawValue: String) {
    EASY("EASY"),
    MEDIUM("MEDIUM"),
    HARD("HARD");

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

enum class MealCategory(val rawValue: String) {
    BREAKFAST("BREAKFAST"),
    LUNCH("LUNCH"),
    BRUNCH("BRUNCH"),
    DINNER("DINNER"),
    BURGER("BURGER"),
    SALAD("SALAD"),
    SOUP("SOUP"),
    APPETIZER("APPETIZER"),
    DESSERT("DESSERT"),
    HOTPOT("HOTPOT"),
    NORTH_VN("NORTH_VN"),
    CENTRAL_VN("CENTRAL_VN"),
    SOUTH_VN("SOUTH_VN"),
    SWEET_SOUP("SWEET_SOUP"),
    VITAMIN("VITAMIN");

    val displayName: String
        get() = when (this) {
            BREAKFAST -> "Breakfast"
            LUNCH -> "Lunch"
            BRUNCH -> "Brunch"
            DINNER -> "Dinner"
            BURGER -> "Burger"
            SALAD -> "Salad"
            SOUP -> "Soup"
            APPETIZER -> "Appetizer"
            DESSERT -> "Dessert"
            HOTPOT -> "Hotpot"
            NORTH_VN -> "North Vietnam"
            CENTRAL_VN -> "Central Vietnam"
            SOUTH_VN -> "South Vietnam"
            SWEET_SOUP -> "Sweet Soup"
            VITAMIN -> "Vitamin"
        }

    val localizationKey: String
        get() = when (this) {
            BREAKFAST -> "category_breakfast"
            LUNCH -> "category_lunch"
            BRUNCH -> "category_brunch"
            DINNER -> "category_dinner"
            BURGER -> "category_burger"
            SALAD -> "category_salad"
            SOUP -> "category_soup"
            APPETIZER -> "category_appetizer"
            DESSERT -> "category_dessert"
            HOTPOT -> "category_hotpot"
            NORTH_VN -> "category_north_vn"
            CENTRAL_VN -> "category_central_vn"
            SOUTH_VN -> "category_south_vn"
            SWEET_SOUP -> "category_sweet_soup"
            VITAMIN -> "category_vitamin"
        }

    val colorRes: Int
        get() = when (this) {
            BREAKFAST -> android.R.color.holo_orange_light
            LUNCH -> android.R.color.holo_blue_light
            BRUNCH -> android.R.color.holo_green_light
            DINNER -> android.R.color.holo_purple
            BURGER -> android.R.color.holo_red_light
            SALAD -> android.R.color.holo_green_dark
            SOUP -> android.R.color.holo_orange_dark
            APPETIZER -> android.R.color.holo_blue_dark
            DESSERT -> android.R.color.holo_red_dark
            HOTPOT -> android.R.color.darker_gray
            NORTH_VN -> android.R.color.holo_green_light
            CENTRAL_VN -> android.R.color.holo_orange_light
            SOUTH_VN -> android.R.color.holo_red_light
            SWEET_SOUP -> android.R.color.holo_purple
            VITAMIN -> android.R.color.holo_green_dark
        }

    fun getDisplayName(language: com.fxanhkhoa.what_to_eat_android.ui.localization.Language, localizationManager: com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager): String {
        return try {
            when (this.localizationKey) {
                "category_breakfast" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.breakfast, language)
                "category_lunch" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.lunch, language)
                "category_brunch" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.brunch, language)
                "category_dinner" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.dinner, language)
                "category_burger" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.burger, language)
                "category_salad" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.salad, language)
                "category_soup" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.soup, language)
                "category_appetizer" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.appetizer, language)
                "category_dessert" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.dessert, language)
                "category_hotpot" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.hotpot, language)
                "category_north_vn" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.north_vn, language)
                "category_central_vn" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.central_vn, language)
                "category_south_vn" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.south_vn, language)
                "category_sweet_soup" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.sweet_soup, language)
                "category_vitamin" -> localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.vitamin, language)
                else -> this.displayName
            }
        } catch (e: Exception) {
            this.displayName
        }
    }

    companion object {
        fun from(value: String?): MealCategory {
            return MealCategory.entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: BREAKFAST
        }

        fun allCategories(): List<MealCategory> = MealCategory.entries

        fun displayNames(): List<String> = MealCategory.entries.map { it.displayName }
    }
}
