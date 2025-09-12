package com.fxanhkhoa.what_to_eat_android.services

import com.fxanhkhoa.what_to_eat_android.model.*
import retrofit2.http.*

interface DishService {
    @GET("dish")
    suspend fun findAll(@QueryMap params: Map<String, @JvmSuppressWildcards Any?>): APIPagination<DishModel>

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

