package com.example.android.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.network.ApiClient
import com.example.android.network.UserService
import com.example.android.network.PhotoService
import com.example.android.repository.UserRepository
import com.example.android.repository.PhotoRepository


class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {

            val retrofit = ApiClient.getRetrofit(context)
            val userService = retrofit.create(UserService::class.java)
            val photoService = retrofit.create(PhotoService::class.java)

            val userRepository = UserRepository(userService)
            val photoRepository = PhotoRepository(photoService)

            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userRepository, photoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}