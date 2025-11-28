package com.example.android.network
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


data class User(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String?,
    val bio: String?,
    val avatarUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
data class RegisterRequest(
    val email : String,
    val username : String,
    val password : String,
    val displayName : String
)

data class RegisterResponse(
    val message: String,
    val error: String?,
    val statusCode : Int
)
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val user:User,
    val accessToken : String
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

}