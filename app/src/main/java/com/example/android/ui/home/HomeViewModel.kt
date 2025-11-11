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

data class PhotoState(
    val photoResId: Int? = null,
    val isLiked: Boolean = false,
    val likeCount: Int = 0 // 초기 좋아요 수
)

class HomeViewModel(private val context: Context) : ViewModel() {

    private val _currentPhotoState: MutableState<PhotoState> = mutableStateOf(PhotoState())
    val currentPhotoState: State<PhotoState> = _currentPhotoState

    fun selectPhoto(resId: Int) {
        Log.d("HomeViewModel", "Selecting photo with ID: $resId")
        _currentPhotoState.value = PhotoState(
            photoResId = resId,
            isLiked = false,
            likeCount = 0
        )
    }

    fun toggleLike() {
        val currentState = _currentPhotoState.value
        val newIsLiked = !currentState.isLiked
        val newLikeCount =
            if (newIsLiked) currentState.likeCount + 1 else currentState.likeCount - 1

        _currentPhotoState.value = currentState.copy(
            isLiked = newIsLiked,
            likeCount = newLikeCount
        )
    }

    fun sharePhoto(photoResId: Int) {
        val imageUri = Uri.parse("android.resource://${context.packageName}/$photoResId")

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, "사진을 공유합니다.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val shareIntent = Intent.createChooser(sendIntent, "사진 공유")
        context.startActivity(shareIntent)
    }
}