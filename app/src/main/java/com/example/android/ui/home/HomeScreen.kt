package com.example.android.ui.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.android.ui.profile.PhotoOverlay
import com.example.android.ui.profile.ProfileViewModel
import com.example.android.ui.profile.ProfileViewModelFactory
import androidx.compose.foundation.clickable

@Composable
fun HomeScreen(profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current)),
               homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current)), // 💡 HomeViewModelFactory가 필요
               onPhotoClick: (Int) -> Unit
) {

    val albumPhotos by profileViewModel.albumPhotos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(bottom = 56.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 사진 그리드
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 이미지 로드(일단 drawable에 있는걸로 로드)
            items(albumPhotos) { photoResId ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { // 💡 클릭 이벤트 처리 추가
                            Log.d("HomeScreen", "Photo clicked: $photoResId")
                            homeViewModel.selectPhoto(photoResId)
                            onPhotoClick(photoResId)
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(photoResId),
                        contentDescription = "Album Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 오버레이
//                    if (photoResId == albumPhotos.firstOrNull()) {
//                        PhotoOverlay(likeCount = 0, daysAgo = 1)
//                    }
                    PhotoOverlay(likeCount = 0, daysAgo = 1)
                }
            }
        }
    }
}