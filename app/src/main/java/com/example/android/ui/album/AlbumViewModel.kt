package com.example.android.ui.album

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.network.AlbumResponse
import com.example.android.network.PhotoResponse
import com.example.android.repository.Album
import com.example.android.repository.AlbumRepository
import kotlinx.coroutines.launch

class AlbumViewModel(
    private val repo: AlbumRepository
) : ViewModel() {

    var albums by mutableStateOf<List<Album>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    fun loadAlbums() = viewModelScope.launch {
        loading = true
        albums = repo.getAlbums()
        loading = false
    }

    fun createAlbum(
        title: String,
        description: String = "",
        visibility: String = "PRIVATE",
        onDone: () -> Unit
    ) = viewModelScope.launch {
        loading = true
        repo.createAlbum(title, description, visibility)
        loadAlbums() // 생성 후 목록 갱신
        loading = false
        onDone()
    }
}