package com.example.android.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.android.R

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainBlueColor = colorResource(id = R.color.main_blue)

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "album",
            onClick = {
                navController.navigate("album") {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(painter = painterResource(id = R.drawable.ic_album), contentDescription = null) },
            label = { Text("Album") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = mainBlueColor,
                selectedTextColor = mainBlueColor
            )
        )

        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = mainBlueColor,
                selectedTextColor = mainBlueColor
            )
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = mainBlueColor,
                selectedTextColor = mainBlueColor
            )
        )
    }
}
