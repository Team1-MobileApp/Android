package com.example.android.ui.album

import android.content.Context
import android.provider.MediaStore
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.android.R
import com.example.android.getAlbumsRootDir
import java.io.File

data class Album(
    val name: String
)

@Composable
fun AlbumScreen(navController: NavController) {
    val context = LocalContext.current
    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var albumName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        albums = loadAlbums(context)
    }

    // "앨범 추가" 버튼을 위해 기존 앨범 + 가짜 항목 하나 더
    val albumsWithAddButton = albums + Album("AddButton")

    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(albumsWithAddButton) { album ->
            if (album.name == "AddButton") {
                // 마지막 "앨범 추가" 버튼
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable {
                            showDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "앨범 추가",
                        modifier = Modifier.size(48.dp),
                        tint = Color.DarkGray
                    )
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(text = "Enter Album Name") },
                        text = {
                            TextField(
                                value = albumName,
                                onValueChange = { albumName = it },
                                label = { Text("Album Name") }
                            )
                        },
                        confirmButton = {
                            Button (onClick = {
                                createAlbumFolder(context, albumName)
                                albums = loadAlbums(context)
                                showDialog = false
                                albumName = ""
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            Button(onClick = {
                                showDialog = false
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            } else {
                // 일반 앨범 항목
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            navController.navigate("albumDetail/${album.name}")
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(R.drawable.ic_album), // 네가 정한 이미지 리소스
                        contentDescription = album.name,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = album.name,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// MediaStore에서 폴더(앨범) 이름만 가져오기
fun loadAlbums(context: Context): List<Album> {
    val rootDir = getAlbumsRootDir(context)
    val folders = rootDir.listFiles { file -> file.isDirectory } ?: arrayOf()
    return folders.map { Album(it.name) }
}

fun createAlbumFolder(context: Context, albumName: String) {
    if (albumName.isBlank()) return

    val rootDir = getAlbumsRootDir(context)
    val newAlbum = File(rootDir, albumName)

    if (!newAlbum.exists()) {
        val created = newAlbum.mkdirs()
        if (created) {
            Toast.makeText(context, "앨범 생성 완료!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "앨범 생성 실패", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "이미 존재하는 앨범 이름입니다", Toast.LENGTH_SHORT).show()
    }
}