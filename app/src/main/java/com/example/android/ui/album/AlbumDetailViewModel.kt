package com.example.android.ui.album

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.network.AlbumResponse
import com.example.android.network.PhotoResponse
import com.example.android.repository.Album
import com.example.android.repository.AlbumRepository
import kotlinx.coroutines.launch

class AlbumDetailViewModel(private val repo: AlbumRepository) : ViewModel() {

    var album = mutableStateOf<AlbumResponse?>(null)

    var photos = mutableStateOf<List<PhotoResponse>>(emptyList())
        private set

    var loading = mutableStateOf(false)
        private set

    fun loadPhotos(albumId: String) = viewModelScope.launch {
        loading.value = true

        val response = repo.getAlbumPhotos(albumId)

        album.value = response.album
        photos.value = response.photos

        loading.value = false
    }

    fun deleteAlbum(
        albumId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) = viewModelScope.launch {
        val result = repo.deleteAlbum(albumId)

        if (result) {
            onSuccess()
        } else {
            onFailure()
        }
    }
}
