package com.fxanhkhoa.what_to_eat_android.services

import com.fxanhkhoa.what_to_eat_android.model.*
import retrofit2.http.*

interface DishService {
    @GET
    suspend fun findAll(@Url url: String): APIPagination<DishModel>

    @GET
    suspend fun searchFuzzy(@Url url: String): APIPagination<DishModel>

    // Keep the original methods as backup
    @GET("dish")
    suspend fun findAllWithQueryMap(@QueryMap params: Map<String, @JvmSuppressWildcards String>): APIPagination<DishModel>

    @GET("dish/search/fuzzy")
    suspend fun searchFuzzyWithQueryMap(@QueryMap params: Map<String, @JvmSuppressWildcards String>): APIPagination<DishModel>

    @GET("dish/slug/{slug}")
    suspend fun findBySlug(@Path("slug") slug: String): DishModel

    @GET("dish/random")
    suspend fun findRandom(
        @Query("limit") limit: Int,
        @Query("mealCategories") mealCategories: List<String>? = null
    ): List<DishModel>

    @POST("dish")
    suspend fun create(@Body dto: CreateDishDto): DishModel

    @PATCH("dish/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: UpdateDishDto): DishModel

    @GET("dish/{id}")
    suspend fun findOne(@Path("id") id: String): DishModel

    @DELETE("dish/{id}")
    suspend fun delete(@Path("id") id: String): DishModel
}
