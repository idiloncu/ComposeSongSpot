package com.example.composesongspot

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.composesongspot.ui.theme.ComposeSongSpotTheme
import com.example.composesongspot.ui.theme.MainView
import com.example.composesongspot.ui.theme.bottom_screen.Home
import com.google.firebase.ktx.BuildConfig
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            Home(navController=rememberNavController())
            ComposeSongSpotTheme {
                window.decorView.apply {
                    systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }

                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.BLACK
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background

                ) {
                    MainView()
                }
            }
        }
    }
}





