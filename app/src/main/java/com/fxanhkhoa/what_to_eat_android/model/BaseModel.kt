package com.fxanhkhoa.what_to_eat_android.model

import com.google.gson.annotations.SerializedName

/**
 * BaseModel equivalent to the provided Swift model.
 */
data class BaseModel(
    @SerializedName("_id")
    val id: String,
    val deleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: String?,
    val updatedBy: String?,
    val deletedBy: String?,
    val deletedAt: String?
)

data class APIPagination<T>(
    // Gson may produce null even for non-nullable Kotlin types; callers must use .orEmpty()
    val data: List<T> = emptyList(),
    val count: Int = 0
)
