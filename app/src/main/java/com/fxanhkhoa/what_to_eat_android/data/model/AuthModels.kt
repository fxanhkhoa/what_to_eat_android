package com.fxanhkhoa.what_to_eat_android.data.model

import User

data class LoginRequest(
    val token: String
)

data class LoginResponse(
    val token: String,
    val refreshToken: String,
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
