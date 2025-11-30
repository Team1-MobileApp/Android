package com.example.android.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

// GET /users/me
data class MyProfileResponse(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val bio: String?,
    val avatarUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val photoCount: Int,
    val receivedLikeCount: Int
)

// GET /users/{id}
data class UserProfileResponse(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String?,
    val avatarUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val photoCount: Int,
    val receivedLikeCount: Int
)

// PATCH /users/me
data class UpdateProfileRequest(
    val displayName: String?,
    val bio: String?,
    val avatarUrl: String?
)

interface UserService{
    @GET("users/me")
    suspend fun getMyProfile() : MyProfileResponse

    @PATCH("users/me")
    suspend fun updateMyProfile(@Body request : UpdateProfileRequest) : MyProfileResponse

    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") userId : String) : UserProfileResponse
}
