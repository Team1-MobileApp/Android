package com.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.rememberNavController
import com.example.android.navigation.NavGraph
import com.example.android.ui.components.BottomBar
import com.example.android.ui.components.TopBar
import com.example.android.ui.theme.AndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTheme {
                val navController = rememberNavController()

                Scaffold(
                    topBar = {
                        TopBar(title = "Four-togenic")
                    },
                    bottomBar = { BottomBar(navController) }
                ) { paddingValues ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}
