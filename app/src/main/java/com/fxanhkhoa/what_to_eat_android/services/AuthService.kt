package com.fxanhkhoa.what_to_eat_android.services

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fxanhkhoa.what_to_eat_android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Data classes

data class ResultToken(val accessToken: String, val refreshToken: String)
data class User(val id: String, val name: String, val email: String, val avatar: String?)

data class LoginRequest(val token: String)
data class RefreshTokenRequest(val refreshToken: String)
data class LogoutRequest(val refreshToken: String)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ResultToken

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): ResultToken

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Any

    @GET("auth/profile")
    suspend fun getProfile(): User
}

class AuthService(context: Context) {
    private val _profile = MutableLiveData<User?>(null)
    val profile: LiveData<User?> = _profile

    private val api: AuthApi

    init {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        api = retrofit.create(AuthApi::class.java)
    }

    suspend fun login(token: String): ResultToken = withContext(Dispatchers.IO) {
        api.login(LoginRequest(token))
    }

    suspend fun refreshToken(refreshToken: String): ResultToken = withContext(Dispatchers.IO) {
        api.refreshToken(RefreshTokenRequest(refreshToken))
    }

    suspend fun logout(refreshToken: String) = withContext(Dispatchers.IO) {
        api.logout(LogoutRequest(refreshToken))
    }

    suspend fun getProfileAPI(): User = withContext(Dispatchers.IO) {
        api.getProfile()
    }

    fun setProfile(profile: User?) {
        _profile.postValue(profile)
    }
}

