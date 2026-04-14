package com.fxanhkhoa.what_to_eat_android.services

import com.fxanhkhoa.what_to_eat_android.model.APIPagination
import com.fxanhkhoa.what_to_eat_android.model.CreateUserDishCollectionDto
import com.fxanhkhoa.what_to_eat_android.model.UpdateUserDishCollectionDto
import com.fxanhkhoa.what_to_eat_android.model.UserDishCollectionModel
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UserDishCollectionService {
    @GET("user-dish-collection")
    suspend fun findAll(@QueryMap params: Map<String, String>): APIPagination<UserDishCollectionModel>

    @GET("user-dish-collection/{id}")
    suspend fun findById(@Path("id") id: String): UserDishCollectionModel

    @POST("user-dish-collection")
    suspend fun create(@Body dto: CreateUserDishCollectionDto): UserDishCollectionModel

    @PUT("user-dish-collection/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: UpdateUserDishCollectionDto): UserDishCollectionModel

    @DELETE("user-dish-collection/{id}")
    suspend fun delete(@Path("id") id: String)
}
