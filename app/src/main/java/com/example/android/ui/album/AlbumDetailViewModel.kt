package com.example.android.ui.album

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.network.PhotoResponse
import com.example.android.repository.Album
import com.example.android.repository.AlbumRepository
import kotlinx.coroutines.launch

class AlbumDetailViewModel(private val repo: AlbumRepository) : ViewModel() {

    var photos = mutableStateOf<List<PhotoResponse>>(emptyList())
        private set

    var loading = mutableStateOf(false)
        private set

    fun loadPhotos(albumId: String) = viewModelScope.launch {
        loading.value = true
        photos.value = repo.getAlbumPhotos(albumId)
        loading.value = false
    }
}
