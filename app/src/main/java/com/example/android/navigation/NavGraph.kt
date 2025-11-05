package com.example.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.android.ui.album.AlbumScreen
import com.example.android.ui.home.HomeScreen
import com.example.android.ui.profile.ProfileScreen
import com.example.android.ui.login.LoginScreen
import com.example.android.ui.login.LoginViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { navController.navigate("home") {
                    // login 화면은 스택에서 제거
                    popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    // 회원가입 화면 구현 필요
                }
            )
        }

        composable("home") { HomeScreen() }
        composable("album") { AlbumScreen() }
        composable("profile") { ProfileScreen() }
    }
}
