package com.example.android.ui.album

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.android.network.AlbumResponse
import com.example.android.network.PhotoResponse
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AlbumDetailScreen(
    navController: NavController,
    albumId: String,
    viewModel: AlbumDetailViewModel,
) {

    val album by viewModel.album
    val photos by viewModel.photos
    val loading by viewModel.loading
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(albumId) {
        viewModel.loadPhotos(albumId)
    }

    // 런타임 권한 처리
    var hasPermission by remember { mutableStateOf(false) }
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) {
            Toast.makeText(context, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 갤러리에서 이미지 선택
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(it, context)
            viewModel.uploadFile(context, file, albumId)
            // 업로드 후 목록 갱신
            viewModel.loadPhotos(albumId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        album?.let { a ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 왼쪽: + 버튼과 앨범 정보
                Row(verticalAlignment = Alignment.CenterVertically) {


                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = "Album Name: ${a.title}")
                        Text(text = "Created At: ${formatIsoDate(a.createdAt)}")
                        Text(text = "Updated At: ${formatIsoDate(a.updatedAt)}")
                    }
                }

                IconButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        hasPermission = true
                    } else {
                        permissionLauncher.launch(permission)
                    }

                    if (hasPermission) {
                        pickImageLauncher.launch("image/*")
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Add, // Material Icons의 + 아이콘
                        contentDescription = "Add Image"
                    )
                }

                // 오른쪽: 삭제 버튼
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Album"
                    )
                }
            }

            Divider(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
            )
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("사진이 없습니다.")
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                items(photos) { photo ->
                    Image(
                        painter = rememberAsyncImagePainter(model = photo.fileUrl),
                        contentDescription = "Photo",
                        modifier = Modifier
                            .padding(8.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                navController.navigate("photoDetail/$albumId/${photo.id}")
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this album?") },
            confirmButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAlbum(
                        albumId = albumId,
                        onSuccess = { navController.popBackStack() },
                        onFailure = { Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show() }
                    )
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Uri -> File 변환
fun uriToFile(uri: Uri, context: Context): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}")
    inputStream?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }
    return tempFile
}

// ISO 문자열 → 보기 좋은 날짜 포맷
fun formatIsoDate(isoString: String?): String {
    return try {
        isoString?.let {
            val parsed = ZonedDateTime.parse(it)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            parsed.format(formatter)
        } ?: ""
    } catch (e: Exception) {
        isoString ?: ""
    }
}
