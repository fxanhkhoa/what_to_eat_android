package com.fxanhkhoa.what_to_eat_android.network

import User
import com.fxanhkhoa.what_to_eat_android.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<Unit>

    @GET("auth/profile")
    suspend fun getProfile(): Response<User>
}
