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
import com.example.android.ui.components.QRScannerScreenWrapper
import com.example.android.ui.home.FullScreenPhotoScreen
import com.example.android.ui.home.HomeScreen
import com.example.android.ui.home.HomeViewModel
import com.example.android.ui.home.HomeViewModelFactory
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
    val context = LocalContext.current
    val retrofit = remember { ApiClient.getRetrofit(context) }
    val photoRepository = remember { PhotoRepository(retrofit.create(PhotoService::class.java)) }
    val homeViewModelFactory = remember { HomeViewModelFactory(context, photoRepository) }

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

        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(
                factory = homeViewModelFactory
            )
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel
            ) }
        composable("album") { AlbumScreen(navController) }
        composable("profile") { ProfileScreen() }
        composable(
            route = "fullScreenPhoto/{photoId}?fileUrl={fileUrl}&isLiked={isLiked}&likeCount={likeCount}",
            arguments = listOf(
                navArgument("photoId") { type = NavType.StringType; nullable = true },
                navArgument("fileUrl") { type = NavType.StringType; nullable = true },
                navArgument("isLiked") { type = NavType.IntType; defaultValue = 0 },
                navArgument("likeCount") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }
            val homeViewModel: HomeViewModel = viewModel(
            viewModelStoreOwner = homeBackStackEntry,
            factory = homeViewModelFactory
            )
            val photoId = backStackEntry.arguments?.getString("photoId")
            val fileUrl = backStackEntry.arguments?.getString("fileUrl")
            val isLiked = (backStackEntry.arguments?.getInt("isLiked") ?: 0) == 1
            val likeCount = backStackEntry.arguments?.getInt("likeCount") ?: 0

            FullScreenPhotoScreen(
                viewModel = homeViewModel,
                photoId = photoId,
                fileUrl = fileUrl,
                isLiked = isLiked,
                likeCount = likeCount,
                onBack = { navController.popBackStack() }
            )


        }
        composable(
            "albumDetail/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->

            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            val context = LocalContext.current

            // Factory를 통해 AlbumDetailViewModel 생성
            val viewModel: AlbumDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AlbumDetailViewModelFactory(context)
            )

            AlbumDetailScreen(
                navController = navController,
                albumId = albumId,
                viewModel = viewModel
            )
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
        composable("qrScanner") {
            QRScannerScreenWrapper() // 단순 QR 스캔 후 브라우저 열기
        }
    }
}
