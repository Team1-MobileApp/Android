package com.example.android.ui.album

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun AlbumPhotoDetailScreen(
    photoId: String,
    viewModel: AlbumPhotoDetailViewModel
) {
    // StateFlow에서 값 가져오기
    val photoDetail by viewModel.photoDetail.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // 화면이 처음 구성될 때 API 호출
    LaunchedEffect(photoId) {
        viewModel.loadPhoto(photoId)
    }

    // 로딩 중 표시
    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // 데이터가 있으면 화면 구성
        photoDetail?.let { detail ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
            ) {
                Text("Photo ID: ${detail.photoId}")
                Text("Visibility: ${detail.visibility}")
                Text("Created At: ${detail.createdAt}")
                Text("Updated At: ${detail.updatedAt}")

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = rememberAsyncImagePainter(detail.fileurl),
                    contentDescription = "Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        } ?: run {
            // 데이터가 없을 경우
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("사진을 불러올 수 없습니다.")
            }
        }
    }
}