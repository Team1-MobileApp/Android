package com.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.android.ui.theme.AndroidTheme

import com.example.android.ui.theme.login.LoginScreen
import com.example.android.ui.theme.login.LoginViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        onLoginSuccess = { userId ->
                             // 로그인 성공 시 home screen으로 넘어가는거 구현해야함
                        },
                        onRegisterClick = {
                            // 회원가입 구현해야함
                        }
                    )
                }
            }
        }
    }
}