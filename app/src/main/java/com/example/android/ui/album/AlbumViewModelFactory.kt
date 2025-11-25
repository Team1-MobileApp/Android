package com.example.android.ui.album

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.network.AlbumService
import com.example.android.network.ApiClient
import com.example.android.network.PhotoService
import com.example.android.repository.AlbumRepository

class AlbumViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val retrofit = ApiClient.getRetrofit(context)
        val api = retrofit.create(AlbumService::class.java)
        val photoApi = retrofit.create(PhotoService::class.java)
        val repo = AlbumRepository(api, photoApi)
        return AlbumViewModel(repo) as T
    }
}