package com.example.android.ui.theme.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import com.example.android.R

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val mainBlueColor = colorResource(id = R.color.main_blue)

    // 자동 로그인 체크
    LaunchedEffect(Unit) {
        viewModel.checkAutoLogin { savedId ->
            onLoginSuccess(savedId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Four-togenic", style = MaterialTheme.typography.headlineLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))


        // 아이디입력
        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("Input your ID.") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Input your password.") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 로그인 버튼
        Button(
            onClick = {
                viewModel.userId = userId
                viewModel.password = password
                viewModel.login(
                    onSuccess = { id -> onLoginSuccess(id) },
                    onError = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = mainBlueColor)
        ) {
            Text("Login", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 회원가입/ 비밀번호 찾기
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onRegisterClick) {
                Text("Register", color = mainBlueColor)
            }
            Text("|", color = mainBlueColor)
            TextButton(onClick = { /*비밀번호 찾기 로직 구현해야함*/ }) {
                Text("Find password", color = mainBlueColor)
            }
        }
    }
}