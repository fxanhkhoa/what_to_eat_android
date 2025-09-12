package com.fxanhkhoa.what_to_eat_android.model

import com.google.gson.annotations.SerializedName

// Model for ingredient in a dish
data class IngredientsInDish(
    val quantity: Double,
    val slug: String,
    val note: String,
    val ingredientId: String
)

// Generic model for multi-language content
// (Assumes you already have this, but included for completeness)
data class MultiLanguage<T>(
    val lang: String,
    val data: T
)

// Main Dish model
// Handles null arrays with default values, and optional fields
// Implements utility functions for localization

data class DishModel(
    @SerializedName("_id")
    val id: String,
    val deleted: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
    val createdBy: String?,
    val updatedBy: String?,
    val deletedBy: String?,
    val deletedAt: String?,

    val slug: String,
    val title: List<MultiLanguage<String>>,
    val shortDescription: List<MultiLanguage<String>>,
    val content: List<MultiLanguage<String>>,
    val tags: List<String> = emptyList(),
    val preparationTime: Int?,
    val cookingTime: Int?,
    val difficultLevel: String?,
    val mealCategories: List<String> = emptyList(),
    val ingredientCategories: List<String> = emptyList(),
    val thumbnail: String?,
    val videos: List<String> = emptyList(),
    val ingredients: List<IngredientsInDish> = emptyList(),
    val relatedDishes: List<String?> = emptyList(),
    val labels: List<String>? = null
) {
    fun getTitle(language: String): String? =
        title.firstOrNull { it.lang == language }?.data

    fun getShortDescription(language: String): String? =
        shortDescription.firstOrNull { it.lang == language }?.data

    fun getContent(language: String): String? =
        content.firstOrNull { it.lang == language }?.data
}

data class CreateDishDto(
    val slug: String,
    val title: List<MultiLanguage<String>>,
    val shortDescription: List<MultiLanguage<String>>,
    val content: List<MultiLanguage<String>>,
    val tags: List<String> = emptyList(),
    val preparationTime: Int? = null,
    val cookingTime: Int? = null,
    val difficultLevel: String? = null,
    val mealCategories: List<String> = emptyList(),
    val ingredientCategories: List<String> = emptyList(),
    val thumbnail: String? = null,
    val videos: List<String> = emptyList(),
    val ingredients: List<IngredientsInDish> = emptyList(),
    val relatedDishes: List<String> = emptyList(),
    val labels: List<String> = emptyList()
)

data class UpdateDishDto(
    val id: String,
    val slug: String,
    val title: List<MultiLanguage<String>>,
    val shortDescription: List<MultiLanguage<String>>,
    val content: List<MultiLanguage<String>>,
    val tags: List<String> = emptyList(),
    val preparationTime: Int? = null,
    val cookingTime: Int? = null,
    val difficultLevel: String? = null,
    val mealCategories: List<String> = emptyList(),
    val ingredientCategories: List<String> = emptyList(),
    val thumbnail: String? = null,
    val videos: List<String> = emptyList(),
    val ingredients: List<IngredientsInDish> = emptyList(),
    val relatedDishes: List<String> = emptyList(),
    val labels: List<String> = emptyList()
)
