package com.example.android.ui.album

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.network.ApiClient
import com.example.android.network.AlbumService
import com.example.android.network.PhotoService
import com.example.android.repository.AlbumRepository
import com.example.android.repository.PhotoRepository

class AlbumDetailViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val retrofit = ApiClient.getRetrofit(context)
        val albumApi = retrofit.create(AlbumService::class.java)
        val photoApi = retrofit.create(PhotoService::class.java)

        val albumRepo = AlbumRepository(albumApi, photoApi)
        val photoRepo = PhotoRepository(photoApi)

        return AlbumDetailViewModel(albumRepo, photoRepo) as T
    }
}