package com.example.android.ui.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import com.example.android.R
import androidx.lifecycle.ViewModelProvider
import com.example.android.network.ApiClient
import com.example.android.network.UserService
import com.example.android.network.PhotoService
import com.example.android.repository.UserRepository
import com.example.android.repository.PhotoRepository

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current))) {
    val uiState = profileViewModel.uiState.value
    val albumPhotos = profileViewModel.albumPhotos.value

    val showDialogFor = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val copiedUri = copyImageToInternalStorage(context, it)
            profileViewModel.updateProfile(profileImageUri = copiedUri)
        }
    }

    when (uiState) {
        ProfileUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("프로필 로딩 중...")
            }
        }
        is ProfileUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("오류 발생: ${uiState.message}")
            }
        }
        is ProfileUiState.Success -> {
            val userProfile = uiState.profile


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F8F8))
                    .padding(bottom = 56.dp)
            ) {
                // 프로필 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 프로필 이미지
                            Image(
                                painter = if (userProfile.avatarUrl != null) {
                                    rememberAsyncImagePainter(Uri.parse(userProfile.avatarUrl))
                                } else {
                                    rememberAsyncImagePainter(R.drawable.ic_profile)
                                },
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                // 이름 수정
                                Text(
                                    text = userProfile.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    modifier = Modifier.clickable {
                                        showDialogFor.value = "name"
                                    }
                                )
                                // 상태메시지 수정
                                Text(
                                    text = userProfile.bio ?: "Hi, how are you :)",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.clickable {
                                        showDialogFor.value = "description"
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // post, like, talk
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStat(userProfile.photoCount.toString(), "post")
                            ProfileStat(userProfile.receivedLikeCount.toString(), "like")
                            ProfileStat("26", "talk")
                        }
                    }
                }

                // 앨범
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(albumPhotos) { photo ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(

                                painter = rememberAsyncImagePainter(photo.imageUrl),
                                contentDescription = "Album Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            PhotoOverlay(likeCount = photo.likeCount, daysAgo = photo.daysAgo)

                        }
                    }
                }
            }
            when (showDialogFor.value) {
                "name" -> {
                    EditProfileDialog(
                        initialValue = userProfile.displayName, // ⭐️ displayName 사용
                        title = "Edit Name",
                        onDismiss = { showDialogFor.value = null },
                        onSave = { newName ->
                            profileViewModel.updateProfile(name = newName) // ⭐️ name으로 update 호출
                        }
                    )
                }
                "description" -> {
                    EditProfileDialog(
                        initialValue = userProfile.bio ?: "", // ⭐️ bio 사용
                        title = "Edit Status Message",
                        onDismiss = { showDialogFor.value = null },
                        onSave = { newDescription ->
                            profileViewModel.updateProfile(description = newDescription) // ⭐️ description으로 update 호출
                        }
                    )
                }
            }
        }
    }
}

// 프로필 이름/상태메시지 편집
@Composable
fun EditProfileDialog(
    initialValue: String,
    title: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val editedValue = remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TextField(
                value = editedValue.value,
                onValueChange = { editedValue.value = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                onSave(editedValue.value)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

fun copyImageToInternalStorage(context: Context, uri: Uri): Uri? {
    val contentResolver = context.contentResolver
    val fileName = "profile_image_${System.currentTimeMillis()}.jpg"
    val destinationFile = File(context.filesDir, fileName)

    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return Uri.fromFile(destinationFile)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

@Composable
fun PhotoOverlay(likeCount: Int, daysAgo: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            // 좋아요 수
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "♥ $likeCount",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            // 시간
            Text(
                text = "$daysAgo days ago",
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}
//
//class PreviewProfileViewModel : ProfileViewModel(
//    // UserRepository에 대한 Dummy 객체 생성
//    userRepository = object : com.example.android.repository.UserRepository(
//        object : com.example.android.network.UserService {
//            // UserService의 모든 suspend 함수를 빈 구현으로 정의합니다.
//            override suspend fun getMyProfile(): com.example.android.network.MyProfileResponse {
//                throw NotImplementedError("Mock implementation")
//            }
//            override suspend fun updateMyProfile(request: com.example.android.network.UpdateProfileRequest): com.example.android.network.MyProfileResponse {
//                throw NotImplementedError("Mock implementation")
//            }
//            override suspend fun getUserProfile(userId: String): com.example.android.network.UserProfileResponse {
//                throw NotImplementedError("Mock implementation")
//            }
//        }
//    ){},
//
//    // PhotoRepository에 대한 Dummy 객체 생성
//    photoRepository = object : com.example.android.repository.PhotoRepository(
//        object : com.example.android.network.PhotoService {
//            // PhotoService의 모든 suspend 함수를 빈 구현으로 정의합니다.
//            // Mocking 시에는 실제 구현이 필요 없으므로 Throw 또는 Do Nothing 처리합니다.
//            override suspend fun uploadPhotoFile(file: okhttp3.MultipartBody.Part, visibility: okhttp3.RequestBody): com.example.android.network.PhotoUploadResponse {
//                throw NotImplementedError("Mock implementation")
//            }
//            override suspend fun getPhotoDetail(photoId: String): com.example.android.network.GetPhotoDetailResponse {
//                throw NotImplementedError("Mock implementation")
//            }
//            override suspend fun addPhotoToAlbum(photoId: String, request: com.example.android.network.AddPhotoToAlbumRequest) {
//                // Do nothing
//            }
//            override suspend fun getMyUploadedPhotos(): List<com.example.android.network.UserPhotoItemResponse> {
//                return emptyList()
//            }
//        }
//    ){}
//) {
//    override val uiState: State<ProfileUiState> = mutableStateOf(
//        ProfileUiState.Success(
//            profile = UserProfile(
//                displayName = "Preview User",
//                bio = "This is a preview status message.",
//                avatarUrl = null,
//                photoCount = 10,
//                receivedLikeCount = 5
//            )
//        )
//    )
//
//    override val albumPhotos: State<List<Photo>> = mutableStateOf(
//        listOf(
//            Photo("1", "https://example.com/photo1", 3, 2),
//            Photo("2", "https://example.com/photo2", 5, 1),
//            Photo("3", "https://example.com/photo3", 1, 10),
//            Photo("4", "https://example.com/photo4", 8, 4),
//        )
//    )
//
//    // Preview에서는 실제 API 호출을 막습니다.
//    override fun loadUserProfile() {}
//    override fun loadAlbumPhotos() {}
//    override fun updateProfile(name: String?, description: String?, profileImageUri: Uri?) {}
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreview() {
//    MaterialTheme {
//        ProfileScreen(profileViewModel = PreviewProfileViewModel())
//    }
//}
