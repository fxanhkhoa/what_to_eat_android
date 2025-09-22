package com.fxanhkhoa.what_to_eat_android.model

import com.google.gson.annotations.SerializedName

// Contact Model
data class Contact(
    @SerializedName("_id") val id: String,
    val email: String,
    val name: String,
    val message: String,
    val deleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null
)

// Create Contact DTO
data class CreateContactDto(
    val email: String,
    val name: String,
    val message: String
)

// Update Contact DTO
data class UpdateContactDto(
    @SerializedName("_id") val id: String,
    val email: String,
    val name: String,
    val message: String
)

// Query Contact DTO
data class QueryContactDto(
    val keyword: String? = null,
    val page: Int = 1,
    val limit: Int = 10,
    val sortField: String? = null,
    val sortOrder: String? = null
) {
    fun toQueryParameters(): Map<String, String> {
        val params = mutableMapOf(
            "page" to page.toString(),
            "limit" to limit.toString()
        )
        keyword?.let { params["keyword"] = it }
        sortField?.let { params["sortField"] = it }
        sortOrder?.let { params["sortOrder"] = it }
        return params
    }
}

// Contact API Response
data class ContactResponse(
    val data: List<Contact>,
    val total: Int,
    val page: Int,
    val limit: Int
)
