package com.example.android.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("album") },
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            label = { Text("Album") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text("Profile") }
        )
    }
}