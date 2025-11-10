package com.example.android.ui.album

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import coil.compose.rememberAsyncImagePainter
import com.example.android.R
import java.io.File

@Composable
fun AlbumPhotoDetailScreen(albumName: String, photoName: String) {
    val context = LocalContext.current
    val albumDir = File(context.filesDir, "four-togenic/$albumName")
    val memo = remember { mutableStateOf("") }

    // 사진 파일 경로
    val photoFile = File(context.filesDir, "four-togenic/$albumName/$photoName")
    val createdAt = remember {
        // 생성일은 기본적으로 접근 불가이므로 폴더의 첫 파일의 생성일, 아니면 lastModified 대안 사용
        val files = albumDir.listFiles { file -> file.isFile }
        files?.minByOrNull { it.lastModified() }?.lastModified() ?: albumDir.lastModified()
    }
    val lastUpdated = File(photoFile.path).lastModified()

    // 날짜 포맷
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val createdAtStr = dateFormat.format(java.util.Date(createdAt))
    val lastUpdatedStr = dateFormat.format(java.util.Date(lastUpdated))

    // 메모 불러오기 (텍스트 파일이 있다면)
    LaunchedEffect(photoFile) {
        val memoFile = File(context.filesDir, "four-togenic/$albumName/${photoName}.txt")
        if (memoFile.exists()) {
            memo.value = memoFile.readText()
        }
    }

    Column(
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
        // 사진 크게 표시
        Image(
            painter = rememberAsyncImagePainter(photoFile),
            contentDescription = photoFile.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 메모 입력
        TextField(
            value = memo.value,
            onValueChange = { memo.value = it },
            placeholder = { Text("Enter a memo") },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        )

        // 저장 버튼
        Button(
            onClick = {
                val memoFile = File(context.filesDir, "four-togenic/$albumName/${photoName}.txt")
                memoFile.writeText(memo.value)
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .height(40.dp) // 높이를 낮게 설정
        ) {
            Text("Save", modifier = Modifier.padding(vertical = 0.dp)) // 텍스트 패딩 제거
        }
    }
}