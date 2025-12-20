package com.fxanhkhoa.what_to_eat_android.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * User model
 * Represents a user in the system with all their profile information
 */
data class UserModel(
    @SerializedName("_id")
    val id: String,
    val _id: String? = null,
    val email: String,
    val password: String? = null,
    val name: String? = null,
    val dateOfBirth: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val googleID: String? = null,
    val facebookID: String? = null,
    val appleID: String? = null,
    val githubID: String? = null,
    val avatar: String? = null,
    val deleted: Boolean? = null,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val roleName: String = "user"
)

/**
 * Query user DTO
 * Used for filtering and searching users
 */
data class QueryUserDto(
    val keyword: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val roleName: List<String>? = null
)

/**
 * Create user DTO
 * Used when creating a new user account
 */
data class CreateUserDto(
    val email: String,
    val password: String? = null,
    val name: String? = null,
    val dateOfBirth: Date? = null,
    val address: String? = null,
    val phone: String? = null,
    val googleID: String? = null,
    val facebookID: String? = null,
    val appleID: String? = null,
    val githubID: String? = null,
    val avatar: String? = null
)

/**
 * Update user DTO
 * Used when updating user profile information
 */
data class UpdateUserDto(
    val id: String,
    val email: String,
    val name: String? = null,
    val dateOfBirth: Date? = null,
    val address: String? = null,
    val phone: String? = null,
    val googleID: String? = null,
    val facebookID: String? = null,
    val appleID: String? = null,
    val githubID: String? = null,
    val avatar: String? = null
)
