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
import com.example.android.ui.album.AlbumDetailViewModel
import com.example.android.ui.album.AlbumDetailViewModelFactory
import com.example.android.ui.album.AlbumPhotoDetailScreen
import com.example.android.ui.album.AlbumPhotoDetailViewModel
import com.example.android.ui.album.AlbumScreen
import com.example.android.ui.components.QRScannerScreen
import com.example.android.ui.components.QRScannerViewModel
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
            "albumDetail/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            val viewModel: AlbumDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AlbumDetailViewModelFactory(LocalContext.current)
            )
            AlbumDetailScreen(navController, albumId, viewModel)
        }


        composable(
            "photoDetail/{photoId}",
            arguments = listOf(navArgument("photoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId")!!
            val viewModel: AlbumPhotoDetailViewModel = viewModel() // 혹은 HiltViewModel

            AlbumPhotoDetailScreen(
                photoId = photoId,
                viewModel = viewModel
            )
        }
        composable("qrScanner") {
            val viewModel: QRScannerViewModel = hiltViewModel() // Hilt 사용 시
            QRScannerScreen(viewModel = viewModel) { result ->
                Log.d("QR", "스캔 결과: $result")
                navController.popBackStack()
            }
        }
    }
}
