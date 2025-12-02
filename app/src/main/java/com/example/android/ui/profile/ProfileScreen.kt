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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.android.repository.PhotoRepository
import com.example.android.ui.home.HomeViewModel
import com.example.android.ui.home.HomeViewModelFactory
import androidx.navigation.NavController
import androidx.compose.runtime.remember

@Composable
fun ProfileScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )

    val retrofit = remember { ApiClient.getRetrofit(context) }
    val photoService = remember { retrofit.create(PhotoService::class.java) }
    val photoRepository = remember { PhotoRepository(photoService) }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            context,
            photoRepository
        )
    )

    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
        profileViewModel.loadPhotos()
    }

    val profileUiState by profileViewModel.uiState
    val userPhotos by profileViewModel.albumPhotos

    val uiState = profileViewModel.uiState.value
    val albumPhotos = profileViewModel.albumPhotos.value

    val showDialogFor = remember { mutableStateOf<String?>(null) }

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

                        // post, like
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStat(userProfile.photoCount.toString(), "post")
                            ProfileStat(userProfile.receivedLikeCount.toString(), "like")
                            //ProfileStat("26", "talk")
                        }
                    }
                }

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
                        val photoUrl = photo.imageUrl ?: ""
                        val likeCount = photo.likeCount
                        val hoursAgo = photo.hoursAgo

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    homeViewModel.selectPhoto(
                                        photoId = photo.id,
                                        fileUrl = photoUrl,
                                        isLiked = true,
                                        initialLikeCount = likeCount,
                                        hoursAgo = hoursAgo
                                    )

                                    val encodedUrl = java.net.URLEncoder.encode(photoUrl, "UTF-8")
                                    val isLikedInt = 1

                                    navController.navigate(
                                        "fullScreenPhoto/${photo.id}?fileUrl=$encodedUrl&isLiked=$isLikedInt&likeCount=$likeCount&hoursAgo=$hoursAgo"
                                    )
                                }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(photo.imageUrl),
                                contentDescription = "My Public Photos",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            PhotoOverlay(isLiked = photo.isLiked, likeCount = photo.likeCount, hoursAgo =photo.hoursAgo)

                        }
                    }
                }
            }
            when (showDialogFor.value) {
                "name" -> {
                    EditProfileDialog(
                        initialValue = userProfile.displayName,
                        title = "Edit Name",
                        onDismiss = { showDialogFor.value = null },
                        onSave = { newName ->
                            profileViewModel.updateProfile(name = newName)
                        }
                    )
                }
                "description" -> {
                    EditProfileDialog(
                        initialValue = userProfile.bio ?: "",
                        title = "Edit Status Message",
                        onDismiss = { showDialogFor.value = null },
                        onSave = { newDescription ->
                            profileViewModel.updateProfile(description = newDescription)
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
fun PhotoOverlay(isLiked:Boolean, likeCount : Int, hoursAgo: Int) {
    val timeText = if (hoursAgo < 24) {
        "${hoursAgo}h ago"
    } else {
        "${hoursAgo / 24}d ago"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        // 좋아요 수 표시
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Likes",
                tint = if (isLiked == true) Color.Red else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$likeCount",
                color = Color.White,
                fontSize = 12.sp
            )
        }

        // 사진 올린 날짜 표시
        Text(
            text = timeText,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}