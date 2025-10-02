package com.fxanhkhoa.what_to_eat_android.data.dto

import kotlin.collections.component1
import kotlin.collections.component2

data class QueryIngredientDto(
    val page: Int = 1,
    val limit: Int = 20,
    val keyword: String? = null,
    val ingredientCategories: List<String>? = null,
) {
    fun toQueryMap(): Map<String, String> {
        val params = mutableMapOf<String, String>()

        params["page"] = page.toString()
        params["limit"] = limit.toString()
        keyword?.let { params["keyword"] = it }

        // For list parameters, we need a different approach
        // Let's use a custom query building method
        return params
    }

    // Build complete URL for ingredient endpoint
    fun buildIngredientUrl(baseUrl: String = "ingredient"): String {
        val queryString = buildQueryString()
        return if (queryString.isNotEmpty()) "$baseUrl?$queryString" else baseUrl
    }

    private fun buildQueryString(): String {
        val baseParams = toQueryMap()
        val queryParts = mutableListOf<String>()

        // Add base parameters
        baseParams.forEach { (key, value) ->
            queryParts.add("$key=$value")
        }

        ingredientCategories?.takeIf { it.isNotEmpty() }?.forEach { category ->
            queryParts.add("ingredientCategory=${java.net.URLEncoder.encode(category, "UTF-8")}")
        }

        return queryParts.joinToString("&")
    }
}