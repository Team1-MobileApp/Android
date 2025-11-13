package com.example.android.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.android.ui.album.AlbumDetailScreen
import com.example.android.ui.album.AlbumPhotoDetailScreen
import com.example.android.ui.album.AlbumScreen
import com.example.android.ui.home.FullScreenPhotoScreen
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

        composable("home") { HomeScreen(
            onPhotoClick = { photoResId ->
                Log.d("NavGraph", "Navigating to fullScreenPhoto with ID: $photoResId")
                navController.navigate("fullScreenPhoto/$photoResId")
            }
        ) }
        composable("album") { AlbumScreen(navController) }
        composable("profile") { ProfileScreen() }
        composable(route = "fullScreenPhoto/{photoResId}",
            arguments = listOf(navArgument("photoResId") { type = NavType.IntType })
        ) { backStackEntry ->
            val photoResId = backStackEntry.arguments?.getInt("photoResId")

            FullScreenPhotoScreen(
                photoResId = photoResId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "albumDetail/{albumName}",
            arguments = listOf(navArgument("albumName") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumName = backStackEntry.arguments?.getString("albumName") ?: ""
            AlbumDetailScreen(navController, albumName)
        }

        composable(
            "photoDetail/{albumName}/{photoName}",
            arguments = listOf(
                navArgument("albumName") { type = NavType.StringType },
                navArgument("photoName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val albumName = backStackEntry.arguments?.getString("albumName")!!
            val photoName = backStackEntry.arguments?.getString("photoName")!!

            AlbumPhotoDetailScreen(
                albumName = albumName,
                photoName = photoName
            )
        }
    }
}
