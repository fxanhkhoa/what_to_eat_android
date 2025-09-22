package com.fxanhkhoa.what_to_eat_android.services

import com.fxanhkhoa.what_to_eat_android.model.*
import retrofit2.http.*

interface ContactService {
    @GET("contact")
    suspend fun findAll(@QueryMap params: Map<String, String>): ContactResponse

    @POST("contact")
    suspend fun create(@Body dto: CreateContactDto): Contact

    @PATCH("contact/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: UpdateContactDto): Contact

    @GET("contact/{id}")
    suspend fun findOne(@Path("id") id: String): Contact

    @DELETE("contact/{id}")
    suspend fun delete(@Path("id") id: String): Contact
}

