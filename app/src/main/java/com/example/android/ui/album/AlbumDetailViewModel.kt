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
        try {
            // 1) 앨범에 포함된 사진 목록 먼저 불러오기
            val response = albumRepo.getAlbumPhotos(albumId)
            val photoList = response.photos

            // 2) 각 사진을 앨범에서 제거 + 사진 자체도 삭제
            for (photo in photoList) {
                // 앨범에서 사진 제거
                photoRepo.deletePhotoFromAlbum(photo.id, albumId)
                // 사진 자체도 삭제
                photoRepo.deletePhoto(photo.id)
            }

            // 3) 모든 사진 정리 후 앨범 자체 삭제
            val result = albumRepo.deleteAlbum(albumId)
            if (result) onSuccess() else onFailure()

        } catch (e: Exception) {
            onFailure()
        }
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
