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
}