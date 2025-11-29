package com.example.android.ui.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.runtime.LaunchedEffect
import com.example.android.network.PhotoService
import com.example.android.repository.PhotoRepository


@Composable
fun FullScreenPhotoScreen(
    photoId: String?,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current, PhotoRepository(
        LocalContext.current as PhotoService
    ))),
    onBack: () -> Unit
) {

    val photoState by homeViewModel.currentPhotoState

    val currentPhotoId = photoId
    val currentPhotoUrl = photoState.fileUrl

    if (currentPhotoId == null || currentPhotoUrl ==null ) {
        Log.d("FullScreen", "Photo ID: null (Navigation Argument Missing)")
        onBack()
        return
    }

    Log.d("FullScreen", "Photo URL: $currentPhotoUrl")

//    LaunchedEffect(photoResId) {
//        homeViewModel.selectPhoto(photoResId)
//    }
//    val photoState by homeViewModel.currentPhotoState
//    val currentPhotoResId = photoState.photoResId
//
//    Log.d("FullScreen", "Photo ID: $currentPhotoResId")


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onBack)
    ) {
        Image(
            painter = rememberAsyncImagePainter(currentPhotoUrl),
            contentDescription = "Full Screen Photo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 8.dp, end = 8.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좋아요 버튼
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable{homeViewModel.toggleLike()}) {
                IconButton(onClick = {homeViewModel.toggleLike()}) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Like",
                        tint = if (photoState.isLiked) Color.Red else Color.White
                    )
                }

                Text(
                    text = "${photoState.likeCount}",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // 댓글 버튼 (댓글 기능 구현할 예정인가?)
            IconButton(onClick = { /* TODO: 댓글 기능 */ }) {
                Icon(
                    Icons.Filled.ModeComment,
                    contentDescription = "Comment",
                    tint = Color.White
                )
            }

            // 공유 버튼
            IconButton(onClick = { homeViewModel.sharePhoto() }) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }
    }
}