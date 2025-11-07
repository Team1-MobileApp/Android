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
import androidx.compose.runtime.rememberUpdatedState
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
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBars = currentRoute != "login"

                // ✅ 1️⃣ 카메라 인텐트 런처 먼저 선언
                val cameraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        // 사진 촬영 성공 시 처리
                    }
                }

                // ✅ 2️⃣ cameraLauncher를 rememberUpdatedState로 참조
                val currentCameraLauncher = rememberUpdatedState(cameraLauncher)

                // ✅ 3️⃣ 권한 요청 런처
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted ->
                        if (granted) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            currentCameraLauncher.value.launch(intent)
                        }
                    }
                )

                // ✅ 버튼 클릭 시 실행할 함수
                val onCameraClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }

                Scaffold(
                    topBar = {
                        if (showBars) TopBar(title = "Four-togenic", onCameraClick = onCameraClick)
                    },
                    bottomBar = {
                        if (showBars) BottomBar(navController)
                    }
                ) { paddingValues ->
                    val isReady = remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
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