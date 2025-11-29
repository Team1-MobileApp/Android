package com.example.android.repository

import com.example.android.network.MyProfileResponse
import com.example.android.network.UpdateProfileRequest
import com.example.android.network.UserProfileResponse
import com.example.android.network.UserService


open class UserRepository(private val userService: UserService) {

    //  내 프로필 조회 (/users/me)
    suspend fun getMyProfile(): Result<MyProfileResponse> = try {
        val response = userService.getMyProfile()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 내 프로필 수정 (PATCH /users/me)
    suspend fun updateMyProfile(request: UpdateProfileRequest): Result<MyProfileResponse> = try {
        val response = userService.updateMyProfile(request)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 사용자 프로필 조회 (/users/{id})
    suspend fun getUserProfile(id: String): Result<UserProfileResponse> = try {
        val response = userService.getUserProfile(id)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
}