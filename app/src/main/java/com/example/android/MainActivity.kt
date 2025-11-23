package com.example.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.ContextCompat
import com.example.android.navigation.NavGraph
import com.example.android.ui.components.BottomBar
import com.example.android.ui.components.TopBar
import com.example.android.ui.theme.AndroidTheme
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTheme {
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    getAlbumsRootDir(context)
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBars = currentRoute != "login"

                // 갤러리 접근 권한 런처 추가
                val galleryPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted ->
                        if (granted) {
                            // TODO: 권한 허용 시 MediaStore에서 이미지 로드
                        } else {
                            // TODO: 권한 거부 시 안내 (ex. Toast)
                        }
                    }
                )

                // 카메라 접근 권한 런처
                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted ->
                        if (!granted) {
                            // 권한 거부 시, 사용자에게 안내가 필요합니다.
                            Log.e("Permission", "카메라 권한이 거부되었습니다. QR 스캔 기능을 사용할 수 없습니다.")
                        }
                    }
                )

                // 버튼 클릭 시 실행할 함수
                val onCameraClick: () -> Unit = {
                    navController.navigate("qrScanner")
                }

                Scaffold(
                    topBar = {
                        if (showBars) {
                            TopBar(
                                title = "Four-togenic",
                                onCameraClick = onCameraClick,
                                showBackButton = currentRoute?.startsWith("albumDetail") == true ||
                                        currentRoute?.startsWith("photoDetail") == true,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBars) BottomBar(navController)
                    }
                ) { paddingValues ->
                    val isReady = remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        // 1. 갤러리 권한 확인 및 요청
                        val hasGalleryPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasGalleryPermission) {
                            galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }

                        // 2. 카메라 권한 확인 및 요청
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }

                        // 권한 요청 후 (사용자 응답 대기 후) UI 표시
                        isReady.value = true
                    }

                    if (isReady.value) {
                        NavGraph(
                            navController = navController,
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

fun getAlbumsRootDir(context: Context): File {
    val rootDir = File(context.filesDir, "four-togenic")
    if (!rootDir.exists()) {
        rootDir.mkdirs()
    }
    return rootDir
}

