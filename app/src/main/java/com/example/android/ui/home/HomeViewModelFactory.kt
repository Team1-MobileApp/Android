package com.example.android.ui.home

import android.content.Context
import com.example.android.repository.PhotoRepository

class HomeViewModelFactory(
    private val context: Context,
    private val photoRepository: PhotoRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(context, photoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}