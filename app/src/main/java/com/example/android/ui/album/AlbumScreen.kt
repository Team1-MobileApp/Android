package com.example.android.ui.album

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.android.R

data class AlbumItem(
    val id: String,
    val title: String
)

@Composable
fun AlbumScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val viewModel: AlbumViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AlbumViewModelFactory(context)
    )

    val albums = viewModel.albums
    val loading = viewModel.loading

    var showDialog by remember { mutableStateOf(false) }
    var albumTitle by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadAlbums()
    }

    // AddButton 항목 추가
    val albumsWithAddButton = albums.map { AlbumItem(it.id.toString(), it.name ?: "제목 없음") } + AlbumItem("AddButton", "Add")


    LazyVerticalGrid(columns = GridCells.Fixed(2),contentPadding = PaddingValues(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)) {

        if (loading) {
            item {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }

        items(albumsWithAddButton) { album ->

            if (album.id == "AddButton") {
                // 추가 버튼
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .aspectRatio(1.33f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable { showDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Album",
                        modifier = Modifier.size(48.dp),
                        tint = Color.DarkGray
                    )
                }

                // 다이얼로그
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Enter Album Name") },
                        text = {
                            TextField(
                                value = albumTitle,
                                onValueChange = { albumTitle = it },
                                label = { Text("Album Name") }
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.createAlbum(
                                    title = albumTitle,
                                    description = "",
                                    visibility = "PRIVATE"
                                ) {
                                    showDialog = false
                                    albumTitle = ""
                                    Toast.makeText(context, "앨범 생성 완료!", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

            } else {
                // 앨범 아이템
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            navController.navigate("albumDetail/${album.id}")
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(R.drawable.ic_album),
                        contentDescription = album.title,
                        modifier = Modifier
                            .aspectRatio(1.33f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = album.title,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
