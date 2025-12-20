package com.fxanhkhoa.what_to_eat_android.network

import android.content.Context
import com.fxanhkhoa.what_to_eat_android.services.DishService
import com.fxanhkhoa.what_to_eat_android.services.DishVoteService
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitProvider {
    private const val BASE_URL = "https://api.eatwhat.io.vn/"

    private var tokenManager: TokenManager? = null
    private var _retrofit: Retrofit? = null

    fun initialize(context: Context) {
        tokenManager = TokenManager.getInstance(context)
        // Force recreation of Retrofit instance with AuthInterceptor
        _retrofit = createRetrofit()
    }

    private fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)

        // Add AuthInterceptor if tokenManager is initialized
        tokenManager?.let { tm ->
            builder.addInterceptor(AuthInterceptor(tm))
        }

        return builder.build()
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    internal val instance: Retrofit
        get() = _retrofit ?: createRetrofit().also { _retrofit = it }

    internal inline fun <reified T> createService(): T = instance.create(T::class.java)

    val dishService: DishService
        get() = createService<DishService>()

    val dishVoteService: DishVoteService
        get() = createService<DishVoteService>()
}
