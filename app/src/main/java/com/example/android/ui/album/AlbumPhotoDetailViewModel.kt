package com.example.android.ui.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.network.GetPhotoDetailResponse
import com.example.android.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlbumPhotoDetailViewModel(
    private val repository: PhotoRepository
) : ViewModel() {

    private val _photoDetail = MutableStateFlow<GetPhotoDetailResponse?>(null)
    val photoDetail: StateFlow<GetPhotoDetailResponse?> = _photoDetail

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadPhoto(photoId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getPhotoDetail(photoId)
                _photoDetail.value = response
            } catch (e: Exception) {
                _photoDetail.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun deletePhoto(
        photoId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) = viewModelScope.launch {
        val result = repository.deletePhoto(photoId)

        if (result) {
            onSuccess()
        } else {
            onFailure()
        }
    }

    fun deletePhotoFromAlbum(
        photoId: String,
        albumId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) = viewModelScope.launch {
        val result = repository.deletePhotoFromAlbum(photoId, albumId)

        if (result) {
            onSuccess()
        } else {
            onFailure()
        }
    }

    fun deletePhotoCompletely(
        photoId: String,
        albumId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) = viewModelScope.launch {

        // 1. 앨범에서 제거
        val removedFromAlbum = repository.deletePhotoFromAlbum(photoId, albumId)

        if (!removedFromAlbum) {
            onFailure()
            return@launch
        }

        // 2. 사진 자체 삭제
        val deleted = repository.deletePhoto(photoId)

        if (deleted) {
            onSuccess()
        } else {
            onFailure()
        }
    }

    fun changeVisibility(photoId: String) = viewModelScope.launch {
        try {
            val currentVisibility = _photoDetail.value?.visibility ?: return@launch
            val newVisibility = if (currentVisibility == "PUBLIC") "PRIVATE" else "PUBLIC"

            // 서버에 PATCH 요청
            repository.changeVisibility(photoId, newVisibility)
            loadPhoto(photoId) // 바뀐 상태를 서버에서 다시 받아서 화면 전체 새로고침
        } catch (e: Exception) {
            // 에러 처리 (로그 등)
        }
    }

}