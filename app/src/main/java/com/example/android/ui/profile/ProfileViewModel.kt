package com.example.android.ui.profile

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.network.MyProfileResponse
import com.example.android.network.UpdateProfileRequest
import com.example.android.repository.UserRepository
import com.example.android.repository.PhotoRepository
import kotlinx.coroutines.launch
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class Photo(
    val id: String,
    val imageUrl: String?,
    val likeCount: Int = 0,
    val daysAgo: Int = 0
)

data class UserProfile(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String? = "Hi, how are you :)",
    val avatarUrl: String? = null,
    val photoCount: Int = 0,
    val receivedLikeCount: Int = 0
)

sealed class ProfileUiState{
    data object Loading : ProfileUiState()
    data class Success(val profile : UserProfile) : ProfileUiState()
    data class Error(val message : String) : ProfileUiState()
}


fun MyProfileResponse.toUserProfile() : UserProfile = UserProfile(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    bio = bio,
    avatarUrl = avatarUrl,
    photoCount = photoCount,
    receivedLikeCount = receivedLikeCount
)

open class ProfileViewModel(
    private val userRepository: UserRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    protected val _uiState: MutableState<ProfileUiState> = mutableStateOf(ProfileUiState.Loading)
    open val uiState: State<ProfileUiState> = _uiState

    protected val _albumPhotos: MutableState<List<Photo>> = mutableStateOf(emptyList())
    open val albumPhotos: State<List<Photo>> = _albumPhotos

    init {
        loadUserProfile()
        loadPhotos()
    }

    open fun loadUserProfile() {
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            userRepository.getMyProfile()
                .onSuccess { response ->
                    _uiState.value = ProfileUiState.Success(response.toUserProfile())
                }.onFailure { e ->
                    _uiState.value = ProfileUiState.Error("프로필 로드 실패: ${e.message}")
                }
        }
    }

//    open fun loadAlbumPhotos() {
//        viewModelScope.launch {
//            photoRepository.getMyUploadedPhotos()
//                .onSuccess { photoResponses ->
//                    _albumPhotos.value = photoResponses.map {
//                        Photo(id = it.id, imageUrl = it.url, likeCount = it.likesCount, daysAgo = it.daysAgo)
//                    }
//                }.onFailure { e ->
//                    _albumPhotos.value = emptyList()
//                }
//        }
//    }

    open fun updateProfile(
        name: String? = null,
        description: String? = null,
        profileImageUri: Uri? = null
    ) {
        val currentProfile = (_uiState.value as? ProfileUiState.Success)?.profile ?: return

        _uiState.value = ProfileUiState.Loading

        val newDisplayName = name ?: currentProfile.displayName
        val newBio = description ?: currentProfile.bio
        var newAvatarUrl = currentProfile.avatarUrl

        viewModelScope.launch {
            if (profileImageUri != null) {
                val file = profileImageUri.path?.let { File(it) }

                if (file != null && file.exists()) {
                    photoRepository.uploadProfileImage(file) // <- PhotoRepository에 추가한 함수 호출
                        .onSuccess { uploadedUrl ->
                            newAvatarUrl = uploadedUrl // 성공 시, 새 웹 URL로 업데이트
                        }.onFailure { e ->
                            println("Image upload failed: ${e.message}")
                            _uiState.value = ProfileUiState.Success(currentProfile)
                            return@launch
                        }
                }
            }

            val request = UpdateProfileRequest(
                displayName = newDisplayName,
                bio = newBio,
                avatarUrl = newAvatarUrl
            )

            userRepository.updateMyProfile(request)
                .onSuccess { response ->
                    _uiState.value = ProfileUiState.Success(response.toUserProfile())
                }.onFailure { e ->
                    _uiState.value = ProfileUiState.Success(currentProfile)
                    println("fail to update profile: ${e.message}")
                }
        }
    }

    open fun loadPhotos(
        limit: Int = 30,
        cursor: String? = null,
        visibility: String = "PUBLIC"
    ) {
        viewModelScope.launch {
            photoRepository.getMyPhoto(limit, cursor, visibility)
                .onSuccess { response ->

                    val photoItems = response.items.map { item ->
                        Photo(
                            id = item.id,
                            imageUrl = item.fileUrl,     // 서버에서 받은 이미지 URL
                            likeCount = 0,               // API에 없어서 0으로 고정
                            daysAgo = calculateDaysAgo(item.createdAt) // createdAt 기반 계산
                        )
                    }

                    _albumPhotos.value = photoItems
                }
                .onFailure { e ->
                    println("Failed to load album photos: ${e.message}")
                    _albumPhotos.value = emptyList()
                }
        }
    }
}

fun calculateDaysAgo(createdAt: String): Int {
    return try {
        val uploadedTime = OffsetDateTime.parse(createdAt)
        val now = OffsetDateTime.now(ZoneId.systemDefault())

        ChronoUnit.DAYS.between(uploadedTime.toLocalDate(), now.toLocalDate()).toInt()
    } catch (e: Exception) {
        0 // 파싱 실패 시 기본값
    }
}