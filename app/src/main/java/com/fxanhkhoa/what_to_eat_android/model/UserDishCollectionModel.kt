package com.fxanhkhoa.what_to_eat_android.model

import com.google.gson.annotations.SerializedName

data class UpdateUserDishCollectionDto(
    val _id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val occasion: String? = null,
    val eventDate: String? = null,
    val dishSlugs: List<String> = emptyList(),
    val tags: List<String>? = null,
    val isPublic: Boolean = false,
    val color: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0
)

data class CreateUserDishCollectionDto(
    val userId: String,
    val name: String,
    val description: String? = null,
    val occasion: String? = null,
    val eventDate: String? = null,
    val dishSlugs: List<String> = emptyList(),
    val tags: List<String>? = null,
    val isPublic: Boolean = false,
    val color: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0
)

data class UserDishCollectionModel(
    @SerializedName("_id")
    val id: String,
    val deleted: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val deletedBy: String? = null,
    val deletedAt: String? = null,
    val userId: String,
    val name: String,
    val description: String? = null,
    val occasion: String? = null,
    val eventDate: String? = null,
    // Gson may deserialize missing/null JSON arrays as null regardless of the Kotlin default,
    // so the type is nullable here and callers should use .orEmpty()
    val dishSlugs: List<String>? = null,
    val tags: List<String>? = null,
    val isPublic: Boolean = false,
    val color: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0
)
