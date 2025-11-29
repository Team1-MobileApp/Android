package com.example.android.ui.home

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.MutableState
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.android.repository.PhotoRepository
import com.example.android.network.UserPhotoItemResponse

data class PhotoState(
    val photoId : String?= null,
    val fileUrl: String? = null,
    val isLiked: Boolean = false,
    val likeCount: Int = 0 // 초기 좋아요 수
)

class HomeViewModel(private val context: Context,private val photoRepository: PhotoRepository) : ViewModel() {

    private val _homePhotos = mutableStateOf<List<UserPhotoItemResponse>>(emptyList())
    val homePhotos : State<List<UserPhotoItemResponse>> = _homePhotos

    private val _currentPhotoState: MutableState<PhotoState> = mutableStateOf(PhotoState())
    val currentPhotoState: State<PhotoState> = _currentPhotoState

    init{
        loadHomePhotos()
    }
    private fun loadHomePhotos() {
        viewModelScope.launch {
            photoRepository.getPublicFeed(sort = "latest", limit = 20)
                .onSuccess {
                    _homePhotos.value = it
                    Log.d("HomeViewModel", "Loaded ${it.size} feed photos")

                    it.forEach { photo ->
                        Log.d("PhotoURLCheck", "Photo ID: ${photo.id}, URL: ${photo.url}")
                    }
                }
                .onFailure {
                    Log.e("HomeViewModel", "Failed to load feed photos: ${it.message}")
                }
        }
    }





    fun selectPhoto(photoId : String, fileUrl: String?, initialIsLiked : Boolean, initialLikeCount : Int) {
        Log.d("HomeViewModel", "Selecting photo with ID: $photoId , FileUrl : $fileUrl")
        _currentPhotoState.value = PhotoState(
            photoId = photoId,
            fileUrl = fileUrl,
            isLiked = initialIsLiked,
            likeCount = initialLikeCount
        )
    }


    fun toggleLike() {
        val currentState = _currentPhotoState.value
        val photoId = currentState.photoId

        if (photoId == null){
            Log.e("HomeViewModel","Cannot toggle like : PhotoId is null")
            return
        }

        viewModelScope.launch{
            val result = if (currentState.isLiked){
                photoRepository.removePhotoLike(photoId)
            }else{
                photoRepository.addPhotoLike(photoId)
            }
            result.onSuccess {
                val newIsLiked = !currentState.isLiked
                val newLikeCount = if (newIsLiked) currentState.likeCount + 1 else currentState.likeCount - 1

                _currentPhotoState.value = currentState.copy(
                    isLiked = newIsLiked,
                    likeCount = newLikeCount
                )
                Log.d("HomeViewModel","Like toggled successfully. isLiked = $newIsLiked, count = $newLikeCount")
            }.onFailure {
                Log.e("HomeViewModel","Failed to toggle like: ${it.message}")
            }
        }
    }


    // 공유 기능 추후 구현해야ㅑㅏㅁ..
    fun sharePhoto() {
        val photoUrl = _currentPhotoState.value.fileUrl

        if (photoUrl.isNullOrEmpty()) {
            Log.e("HomeViewModel", "Cannot share photo: fileUrl is null or empty. Please select a valid photo.")
            return
        }

        val imageUri = Uri.parse(photoUrl)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, "이 사진을 공유합니다: $photoUrl")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val shareIntent = Intent.createChooser(sendIntent, "사진 공유")
        context.startActivity(shareIntent)
    }
}