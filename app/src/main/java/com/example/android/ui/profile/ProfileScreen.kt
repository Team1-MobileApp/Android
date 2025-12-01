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

                // 사진 띄우기
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