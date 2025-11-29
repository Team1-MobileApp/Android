package com.example.android.ui.album

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.network.AlbumResponse
import com.example.android.network.PhotoResponse
import com.example.android.repository.AlbumRepository
import com.example.android.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AlbumDetailViewModel(
    private val albumRepo: AlbumRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {

    var album = mutableStateOf<AlbumResponse?>(null)
    var photos = mutableStateOf<List<PhotoResponse>>(emptyList())
        private set
    var loading = mutableStateOf(false)
        private set

    // 앨범 내 사진 불러오기
    fun loadPhotos(albumId: String) = viewModelScope.launch {
        loading.value = true
        val response = albumRepo.getAlbumPhotos(albumId)
        album.value = response.album
        photos.value = response.photos
        loading.value = false
    }

    // 앨범 삭제
    fun deleteAlbum(
        albumId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) = viewModelScope.launch {
        val result = albumRepo.deleteAlbum(albumId)
        if (result) onSuccess() else onFailure()
    }

    // 사진 업로드
    fun uploadFile(context: Context, file: File, albumId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Photo 파일 업로드
                val uploaded = photoRepo.uploadPhotoFile(file)

                // 업로드 후 앨범에 사진 추가
                photoRepo.addPhotoToAlbum(
                    photoId = uploaded.photoId,
                    albumId = albumId,
                    visibility = "PRIVATE"
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "업로드 완료!", Toast.LENGTH_SHORT).show()
                    loadPhotos(albumId) // 업로드 후 목록 갱신
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
