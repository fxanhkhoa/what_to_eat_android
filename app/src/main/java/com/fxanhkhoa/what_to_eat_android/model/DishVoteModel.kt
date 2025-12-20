package com.fxanhkhoa.what_to_eat_android.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

// MARK: - DishVoteItem
data class DishVoteItem(
    val slug: String,
    val customTitle: String? = null,
    val voteUser: List<String> = emptyList(),
    val voteAnonymous: List<String> = emptyList(),
    val isCustom: Boolean = false
) {
    // Generate a unique ID for each instance (similar to Swift's UUID)
    val id: String = UUID.randomUUID().toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DishVoteItem

        if (slug != other.slug) return false
        if (customTitle != other.customTitle) return false
        if (isCustom != other.isCustom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slug.hashCode()
        result = 31 * result + (customTitle?.hashCode() ?: 0)
        result = 31 * result + isCustom.hashCode()
        return result
    }
}

// MARK: - DishVote
data class DishVoteModel(
    @SerializedName("_id")
    val id: String,
    val deleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: String?,
    val updatedBy: String?,
    val deletedBy: String?,
    val deletedAt: String?,
    val title: String,
    val description: String,
    val dishVoteItems: List<DishVoteItem>
)

// MARK: - DishVoteFilter
data class DishVoteFilter(
    val keyword: String? = null,
    val page: Int? = null,
    val limit: Int? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null
)

// MARK: - CreateDishVoteDto
data class CreateDishVoteDto(
    val title: String? = null,
    val description: String? = null,
    val dishVoteItems: List<DishVoteItem>
)

// MARK: - UpdateDishVoteDto
data class UpdateDishVoteDto(
    @SerializedName("_id")
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val dishVoteItems: List<DishVoteItem>
)

// MARK: - VoteDishDto
data class VoteDishDto(
    val slug: String,
    val myName: String,
    val userID: String?,
    val isVoting: Boolean
)
