package com.example.android.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.android.network.AlbumService
import com.example.android.network.ApiClient
import com.example.android.network.PhotoService
import com.example.android.repository.AlbumRepository
import com.example.android.repository.PhotoRepository
import com.example.android.ui.album.AlbumDetailScreen
import com.example.android.ui.album.AlbumDetailViewModel
import com.example.android.ui.album.AlbumDetailViewModelFactory
import com.example.android.ui.album.AlbumPhotoDetailScreen
import com.example.android.ui.album.AlbumPhotoDetailViewModel
import com.example.android.ui.album.AlbumPhotoDetailViewModelFactory
import com.example.android.ui.album.AlbumScreen
import com.example.android.ui.components.QRScannerScreen
import com.example.android.ui.components.QRScannerViewModel
import com.example.android.ui.components.QRScannerViewModelFactory
import com.example.android.ui.home.FullScreenPhotoScreen
import com.example.android.ui.home.HomeScreen
import com.example.android.ui.profile.ProfileScreen
import com.example.android.ui.login.LoginScreen
import com.example.android.ui.login.LoginViewModel
import com.example.android.ui.register.RegisterScreen
import com.example.android.ui.register.RegisterViewModel

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
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            val registerViewModel: RegisterViewModel = viewModel()

            RegisterScreen(
                viewModel = registerViewModel,
                onRegistrationSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
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
            route = "photoDetail/{albumId}/{photoId}",
            arguments = listOf(
                navArgument("albumId") { type = NavType.StringType },
                navArgument("photoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val albumId = backStackEntry.arguments?.getString("albumId")!!
            val photoId = backStackEntry.arguments?.getString("photoId")!!

            val context = LocalContext.current
            val retrofit = ApiClient.getRetrofit(context)

            val viewModel: AlbumPhotoDetailViewModel = viewModel(
                backStackEntry,
                factory = AlbumPhotoDetailViewModelFactory(
                    PhotoRepository(
                        retrofit.create(PhotoService::class.java)
                    )
                )
            )

            AlbumPhotoDetailScreen(
                albumId = albumId,
                photoId = photoId,
                navController = navController,
                viewModel = viewModel
            )
        }
        composable("qrScanner") { backStackEntry ->

            val context = LocalContext.current
            val retrofit = ApiClient.getRetrofit(context)
            val viewModel: QRScannerViewModel = viewModel(
                backStackEntry,
                factory = QRScannerViewModelFactory(
                    AlbumRepository(
                        retrofit.create(AlbumService::class.java),
                        retrofit.create(PhotoService::class.java)
                    )
                )
            )

            QRScannerScreen(viewModel = viewModel) { result ->
                Log.d("QR", "스캔 결과: $result")
                navController.popBackStack()
            }
        }
    }
}
