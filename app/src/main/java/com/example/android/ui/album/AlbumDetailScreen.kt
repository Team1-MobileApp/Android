package com.example.android.ui.album

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter


@Composable
fun AlbumDetailScreen(navController: NavController, albumId: String, viewModel: AlbumDetailViewModel) {

    val photos = viewModel.photos.value
    val loading = viewModel.loading.value

    LaunchedEffect(albumId) {
        viewModel.loadPhotos(albumId)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
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
                items(items = photos) { photo ->
                    Image(
                        painter = rememberAsyncImagePainter(photo.url),
                        contentDescription = "Photo",
                        modifier = Modifier
                            .padding(8.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                navController.navigate("photoDetail/${albumId}/${photo.id}")
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}