package net.serverpeon.discord.internal.rest.retro

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Auth {
    @POST("auth/login")
    fun login(@Body data: LoginRequest): Call<LoginResponse>

    data class LoginRequest(val email: String, val password: String)

    data class LoginResponse(val token: String?)

    @POST("auth/logout")
    fun logout(@Body data: LogoutRequest): Call<Void>

    data class LogoutRequest(val token: String)
}