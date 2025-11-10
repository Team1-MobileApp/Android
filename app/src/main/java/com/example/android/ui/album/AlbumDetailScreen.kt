package com.example.android.ui.album

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.android.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlbumDetailScreen(navController: NavController, albumName: String) {
    val context = LocalContext.current
    val photos = remember { mutableStateOf<List<File>>(emptyList()) }
    val albumDir = File(context.filesDir, "four-togenic/$albumName")

    // 폴더 정보 추출
    val createdAt = remember {
        // 생성일은 기본적으로 접근 불가이므로 폴더의 첫 파일의 생성일, 아니면 lastModified 대안 사용
        val files = albumDir.listFiles { file -> file.isFile }
        files?.minByOrNull { it.lastModified() }?.lastModified() ?: albumDir.lastModified()
    }
    val lastUpdated = albumDir.lastModified()

    // 날짜 포맷
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val createdAtStr = dateFormat.format(Date(createdAt))
    val lastUpdatedStr = dateFormat.format(Date(lastUpdated))

    LaunchedEffect(Unit) {
        photos.value = albumDir.listFiles { file -> file.isFile }?.toList() ?: emptyList()
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 상단 앨범 정보
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_album),
                contentDescription = "Album Icon",
                tint = Color(0xFF90A4AE),
                modifier = Modifier.size(84.dp).padding(end = 16.dp)
            )
            Column {
                Text("Album Name: $albumName", fontWeight = FontWeight.Bold)
                Text("Created At: $createdAtStr")
                Text("Last Updated: $lastUpdatedStr")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (photos.value.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("사진이 없습니다.")
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                items(photos.value) { photo ->
                    Image(
                        painter = rememberAsyncImagePainter(photo),
                        contentDescription = photo.name,
                        modifier = Modifier
                            .padding(8.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                // NavController를 통해 AlbumPhotoDetailScreen으로 이동
                                navController.navigate("photoDetail/${albumName}/${photo.name}")
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}