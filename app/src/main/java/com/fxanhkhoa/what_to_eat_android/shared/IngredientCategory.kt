package com.fxanhkhoa.what_to_eat_android.shared

import androidx.compose.ui.graphics.Color
import com.fxanhkhoa.what_to_eat_android.R

enum class IngredientCategory(
    val displayName: String,
    val iconName: Int,
    val color: Color,
    val localizationKey: Int
) {
    MILK_AND_DAIRY("Milk and Dairy", R.drawable.milk_and_dairy, Color(0xFF2196F3), R.string.milk_and_dairy),
    GRAINS("Grains", R.drawable.grains, Color(0xFFFFC107), R.string.grains),
    BEVERAGES_NONALCOHOLIC("Beverages Nonalcoholic", R.drawable.beverages_nonalcoholic, Color(0xFF90CAF9), R.string.beverages_nonalcoholic),
    PROTEIN_FOODS("Protein Foods", R.drawable.protein_foods, Color(0xFF4CAF50), R.string.protein_foods),
    SNACKS_AND_SWEETS("Snacks and Sweets", R.drawable.snacks_and_sweets, Color(0xFFFFB300), R.string.snacks_and_sweets),
    ALCOHOLIC_BEVERAGES("Alcoholic Beverages", R.drawable.alcoholic_beverages, Color(0xFF8D6E63), R.string.alcoholic_beverages),
    WATER("Water", R.drawable.water, Color(0xFF00B8D4), R.string.water),
    FATS_AND_OILS("Fats and Oils", R.drawable.fats_and_oils, Color(0xFFFF7043), R.string.fats_and_oils),
    MIXED_DISHES("Mixed Dishes", R.drawable.mixed_dishes, Color(0xFFBDBDBD), R.string.mixed_dishes),
    FRUIT("Fruit", R.drawable.fruit, Color(0xFFFF9800), R.string.fruit),
    CONDIMENTS_AND_SAUCES("Condiments and Sauces", R.drawable.condiments_and_sauces, Color(0xFFBA68C8), R.string.condiments_and_sauces),
    SUGARS("Sugars", R.drawable.sugars, Color(0xFFFFF176), R.string.sugars),
    VEGETABLES("Vegetables", R.drawable.vegetables, Color(0xFF8BC34A), R.string.vegetables),
    HERBS_AND_SPICES("Herbs and Spices", R.drawable.herbs_and_spices, Color(0xFF43A047), R.string.herbs_and_spices),
    NUTS_AND_SEEDS("Nuts and Seeds", R.drawable.nuts_and_seeds, Color(0xFF795548), R.string.nuts_and_seeds),
    BAKING_INGREDIENTS("Baking Ingredients", R.drawable.baking_ingredients, Color(0xFFD7CCC8), R.string.baking_ingredients),
    CANNED_AND_PRESERVED("Canned and Preserved", R.drawable.canned_and_preserved, Color(0xFF607D8B), R.string.canned_and_preserved),
    INFANT_FORMULA_AND_BABY_FOOD("Infant Formula and Baby Food", R.drawable.infant_formula_and_baby_food, Color(0xFFFFCDD2), R.string.infant_formula_and_baby_food),
    OTHER("Other", R.drawable.other, Color(0xFF9E9E9E), R.string.other);


    fun getDisplayName(localizationManager: com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager, language: com.fxanhkhoa.what_to_eat_android.ui.localization.Language): String {
        return localizationManager.getString(this.localizationKey, language)
    }

    companion object {
        val allCases = IngredientCategory.entries

        fun fromString(categoryString: String): IngredientCategory {
            return try {
                // Try to match by enum name first
                valueOf(categoryString.uppercase().replace(" ", "_"))
            } catch (e: IllegalArgumentException) {
                // If enum name doesn't match, try to match by display name
                entries.find {
                    it.displayName.equals(categoryString, ignoreCase = true) ||
                    it.name.equals(categoryString.replace(" ", "_"), ignoreCase = true)
                } ?: OTHER
            }
        }
    }
}