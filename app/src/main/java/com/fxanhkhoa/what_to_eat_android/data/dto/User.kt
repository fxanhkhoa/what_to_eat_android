package com.fxanhkhoa.what_to_eat_android.data.dto

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val email: String,
    val avatar: String?,
    val googleID: String? = null,
    val roleName: String? = null,
    val dateOfBirth: String? = null,
    val deleted: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
