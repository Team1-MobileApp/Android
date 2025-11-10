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

                // 카메라 인텐트 런처 선언
                val cameraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        // 사진 촬영 성공 시 처리
                    }
                }

                // cameraLauncher를 rememberUpdatedState로 참조
                val currentCameraLauncher = rememberUpdatedState(cameraLauncher)

                // 권한 요청 런처
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted ->
                        if (granted) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            currentCameraLauncher.value.launch(intent)
                        }
                    }
                )

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

                // 버튼 클릭 시 실행할 함수
                val onCameraClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
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
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }

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