package com.fxanhkhoa.what_to_eat_android.services

import com.fxanhkhoa.what_to_eat_android.model.*
import retrofit2.http.*

interface DishVoteService {
    @GET("dish-vote")
    suspend fun findAll(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
        @Query("keyword") keyword: String? = null
    ): APIPagination<DishVoteModel>

    @GET("dish-vote/{id}")
    suspend fun findById(@Path("id") id: String): DishVoteModel

    @POST("dish-vote")
    suspend fun create(@Body dto: CreateDishVoteDto): DishVoteModel

    @PATCH("dish-vote/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: UpdateDishVoteDto
    ): DishVoteModel

    @DELETE("dish-vote/{id}")
    suspend fun delete(@Path("id") id: String): DishVoteModel

    @POST("dish-vote/vote")
    suspend fun vote(@Body dto: VoteDishDto): DishVoteModel

    @GET("dish-vote/share/{shareLink}")
    suspend fun findByShareLink(@Path("shareLink") shareLink: String): DishVoteModel
}
