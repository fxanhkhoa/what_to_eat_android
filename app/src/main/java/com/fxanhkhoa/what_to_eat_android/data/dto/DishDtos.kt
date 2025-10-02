package com.fxanhkhoa.what_to_eat_android.data.dto

import com.fxanhkhoa.what_to_eat_android.model.MultiLanguage

data class QueryDishDto(
    val page: Int = 1,
    val limit: Int = 20,
    val keyword: String? = null,
    val tags: List<String>? = null,
    val preparationTimeFrom: Int? = null,
    val preparationTimeTo: Int? = null,
    val cookingTimeFrom: Int? = null,
    val cookingTimeTo: Int? = null,
    val difficultLevels: List<String>? = null,
    val mealCategories: List<String>? = null,
    val ingredientCategories: List<String>? = null,
    val ingredients: List<String>? = null,
    val labels: List<String>? = null
) {
    fun toQueryMap(): Map<String, String> {
        val params = mutableMapOf<String, String>()

        params["page"] = page.toString()
        params["limit"] = limit.toString()
        keyword?.let { params["keyword"] = it }

        preparationTimeFrom?.let { params["preparationTimeFrom"] = it.toString() }
        preparationTimeTo?.let { params["preparationTimeTo"] = it.toString() }
        cookingTimeFrom?.let { params["cookingTimeFrom"] = it.toString() }
        cookingTimeTo?.let { params["cookingTimeTo"] = it.toString() }

        // For list parameters, we need a different approach
        // Let's use a custom query building method
        return params
    }

    // Build complete URL for dish endpoint
    fun buildDishUrl(baseUrl: String = "dish"): String {
        val queryString = buildQueryString()
        return if (queryString.isNotEmpty()) "$baseUrl?$queryString" else baseUrl
    }

    // Build complete URL for search/fuzzy endpoint
    fun buildSearchFuzzyUrl(baseUrl: String = "dish/search/fuzzy"): String {
        val queryString = buildQueryString()
        return if (queryString.isNotEmpty()) "$baseUrl?$queryString" else baseUrl
    }

    // Custom method to build query string manually for list parameters
    private fun buildQueryString(): String {
        val baseParams = toQueryMap()
        val queryParts = mutableListOf<String>()

        // Add base parameters
        baseParams.forEach { (key, value) ->
            queryParts.add("$key=$value")
        }

        // Add list parameters manually - each item gets its own parameter
        tags?.takeIf { it.isNotEmpty() }?.forEach { tag ->
            queryParts.add("tags=${java.net.URLEncoder.encode(tag, "UTF-8")}")
        }

        difficultLevels?.takeIf { it.isNotEmpty() }?.forEach { level ->
            queryParts.add("difficultLevels=${java.net.URLEncoder.encode(level, "UTF-8")}")
        }

        mealCategories?.takeIf { it.isNotEmpty() }?.forEach { category ->
            queryParts.add("mealCategories=${java.net.URLEncoder.encode(category, "UTF-8")}")
        }

        ingredientCategories?.takeIf { it.isNotEmpty() }?.forEach { category ->
            queryParts.add("ingredientCategories=${java.net.URLEncoder.encode(category, "UTF-8")}")
        }

        ingredients?.takeIf { it.isNotEmpty() }?.forEach { ingredient ->
            queryParts.add("ingredients=${java.net.URLEncoder.encode(ingredient, "UTF-8")}")
        }

        labels?.takeIf { it.isNotEmpty() }?.forEach { label ->
            queryParts.add("labels=${java.net.URLEncoder.encode(label, "UTF-8")}")
        }

        return queryParts.joinToString("&")
    }
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
    val ingredients: List<IngredientsInDishDto> = emptyList(),
    val relatedDishes: List<String> = emptyList(),
    val labels: List<String>? = null
)

data class UpdateDishDto(
    val slug: String? = null,
    val title: List<MultiLanguage<String>>? = null,
    val shortDescription: List<MultiLanguage<String>>? = null,
    val content: List<MultiLanguage<String>>? = null,
    val tags: List<String>? = null,
    val preparationTime: Int? = null,
    val cookingTime: Int? = null,
    val difficultLevel: String? = null,
    val mealCategories: List<String>? = null,
    val ingredientCategories: List<String>? = null,
    val thumbnail: String? = null,
    val videos: List<String>? = null,
    val ingredients: List<IngredientsInDishDto>? = null,
    val relatedDishes: List<String>? = null,
    val labels: List<String>? = null
)

data class IngredientsInDishDto(
    val quantity: Double,
    val slug: String,
    val note: String,
    val ingredientId: String
)
