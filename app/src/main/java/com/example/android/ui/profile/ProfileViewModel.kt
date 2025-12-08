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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.Duration

data class Photo(
    val id: String,
    val imageUrl: String?,
    val likeCount: Int = 0,
    val isLiked : Boolean,
    val hoursAgo: Int = 0
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
        profileImageUri: Uri? = null,
        avatarUrlInput: String? = null // 선택적 URL 입력
    ) {
        val currentProfile = (_uiState.value as? ProfileUiState.Success)?.profile ?: return

        _uiState.value = ProfileUiState.Loading

        viewModelScope.launch {
            try {
                val displayNamePart = (name ?: currentProfile.displayName)
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val bioPart = (description ?: currentProfile.bio ?: "")
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val avatarUrlPart = (avatarUrlInput ?: currentProfile.avatarUrl ?: "")
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                // 이미지 파일 파트
                var avatarPart: MultipartBody.Part? = null
                if (profileImageUri != null) {
                    val file = File(profileImageUri.path!!)
                    if (file.exists()) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        avatarPart = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                    }
                }

                val result = userRepository.updateMyProfile(
                    avatarPart,
                    displayNamePart,
                    bioPart,
                    avatarUrlPart
                )

                _uiState.value = ProfileUiState.Success(result.getOrThrow().toUserProfile())

            } catch (e: Exception) {
                println("fail to update profile: ${e.message}")
                _uiState.value = ProfileUiState.Error("프로필 수정 실패: ${e.message}")
            }
        }
    }

    open fun loadPhotos(
        limit: Int = 100,
        cursor: String? = null,
        visibility: String = "PUBLIC"
    ) {
        viewModelScope.launch {
            photoRepository.getMyPhoto(limit, cursor, visibility)
                .onSuccess { response ->

                    val photoItems = response.items.map { item ->
                        Photo(
                            id = item.id,
                            imageUrl = item.fileUrl,
                            likeCount = item.likesCount,
                            isLiked = item.isLiked,
                            hoursAgo = calculateTimeAgoInHours(item.createdAt)
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
fun calculateTimeAgoInHours(createdAt: String): Int {
    return try {
        val uploadedTime = OffsetDateTime.parse(createdAt)
        val now = OffsetDateTime.now(ZoneId.systemDefault())

        val duration = Duration.between(uploadedTime, now)
        duration.toHours().toInt()
    } catch (e: Exception) {
        0
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