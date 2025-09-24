package com.fxanhkhoa.what_to_eat_android.services

import com.fxanhkhoa.what_to_eat_android.model.*
import retrofit2.http.*

interface IngredientService {
    @GET("ingredient")
    suspend fun findAll(@QueryMap params: Map<String, @JvmSuppressWildcards Any?>): APIPagination<Ingredient>

    @GET("ingredient/random")
    suspend fun findRandom(
        @Query("limit") limit: Int,
        @Query("ingredientCategory") ingredientCategory: List<String>? = null
    ): List<Ingredient>

    @POST("ingredient")
    suspend fun create(@Body dto: CreateIngredientDto): Ingredient

    @PATCH("ingredient/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: UpdateIngredientDto): Ingredient

    @GET("ingredient/{id}")
    suspend fun findOne(@Path("id") id: String): Ingredient

    @DELETE("ingredient/{id}")
    suspend fun delete(@Path("id") id: String): Ingredient
}
