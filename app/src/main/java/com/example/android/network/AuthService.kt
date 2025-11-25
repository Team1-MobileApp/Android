package com.example.android.network
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


data class RegisterRequest(
    val email : String,
    val username : String,
    val password : String,
    val displayName : String
)

data class RegisterResponse(
    val success : Boolean,
    val message : String,
    val email : String?
)
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success :Boolean,
    val message : String,
    val token : String?,
    val Id : String?
)

interface AuthService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @FormUrlEncoded
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Field("refreshToken") refreshToken: String
    ): LoginResponse
}