package com.fxanhkhoa.what_to_eat_android.model

import com.google.gson.annotations.SerializedName

data class Ingredient(
    @SerializedName("_id")
    val id: String,
    val deleted: Boolean,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val deletedBy: String? = null,
    val deletedAt: String? = null,
    val slug: String,
    val title: List<MultiLanguage<String>>,
    val measure: String? = null,
    val calories: Double? = null,
    val carbohydrate: Double? = null,
    val fat: Double? = null,
    val ingredientCategory: List<String> = emptyList(),
    val weight: Double? = null,
    val protein: Double? = null,
    val cholesterol: Double? = null,
    val sodium: Double? = null,
    val images: List<String> = emptyList()
) {
    fun getTitle(language: String): String? =
        title.firstOrNull { it.lang == language }?.data
}

data class CreateIngredientDto(
    val slug: String,
    val title: List<MultiLanguage<String>>,
    val measure: String? = null,
    val calories: Double? = null,
    val carbohydrate: Double? = null,
    val fat: Double? = null,
    val ingredientCategory: List<String> = emptyList(),
    val weight: Double? = null,
    val protein: Double? = null,
    val cholesterol: Double? = null,
    val sodium: Double? = null,
    val images: List<String> = emptyList()
)

data class UpdateIngredientDto(
    val id: String,
    val slug: String,
    val title: List<MultiLanguage<String>>,
    val measure: String? = null,
    val calories: Double? = null,
    val carbohydrate: Double? = null,
    val fat: Double? = null,
    val ingredientCategory: List<String> = emptyList(),
    val weight: Double? = null,
    val protein: Double? = null,
    val cholesterol: Double? = null,
    val sodium: Double? = null,
    val images: List<String> = emptyList()
)
