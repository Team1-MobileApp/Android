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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import com.example.android.repository.PhotoRepository
import com.example.android.network.PhotoService
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Composable
fun HomeScreen(
    //onPhotoClick: (String) -> Unit
    navController: NavController,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current

    val client = remember {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }
    val photoService = remember {
        Retrofit.Builder()
            .baseUrl("https://fourtogenic-server-production.up.railway.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PhotoService::class.java)
    }
    val photoRepository = remember { PhotoRepository(photoService) }
    val factory = remember { HomeViewModelFactory(context, photoRepository) }

    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val homePhotos by homeViewModel.homePhotos


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
            items(homePhotos) { photo ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            photo.id.let { id ->
                                val url = photo.url.orEmpty()
                                val likeCount = photo.likesCount

                                val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                                val isLikedInt = if (photo.isLiked == true) 1 else 0
                                navController.navigate(
                                    "fullScreenPhoto/$id?fileUrl=$encodedUrl&isLiked=$isLikedInt&likeCount=$likeCount"
                                )

                                Log.d("NavGraph", "Navigating to fullScreenPhoto with ID: $id")
                            }
                        }
                ) {
                    val painter = rememberAsyncImagePainter(photo.url)
                    val isLoading = painter.state is AsyncImagePainter.State.Loading
                    val isError = painter.state is AsyncImagePainter.State.Error || photo.url == null

                    if (isLoading || isError){
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ){
                            Text(text = "No Image",color = Color.White)
                        }
                    }
                    if (photo.url!=null){
                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    PhotoOverlay(photo.isLiked,photo.likesCount, photo.daysAgo)
                }

            }
        }

    }
}

@Composable
fun PhotoOverlay(isLiked : Boolean?, likeCount: Int, daysAgo: Int) {
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
            text = "${daysAgo}d ago",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}