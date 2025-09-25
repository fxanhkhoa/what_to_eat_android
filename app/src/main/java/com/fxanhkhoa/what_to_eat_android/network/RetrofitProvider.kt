package com.fxanhkhoa.what_to_eat_android.network

import com.fxanhkhoa.what_to_eat_android.services.DishService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitProvider {
    private const val BASE_URL = "https://api.eatwhat.io.vn/"

    var okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
        .readTimeout(30, TimeUnit.SECONDS) // Set read timeout
        .writeTimeout(30, TimeUnit.SECONDS) // Set write timeout
        // Optionally set call timeout
        .callTimeout(60, TimeUnit.SECONDS) // Set overall call timeout
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    inline fun <reified T> createService(): T = instance.create(T::class.java)

    val dishService: DishService by lazy { createService<DishService>() }
}
