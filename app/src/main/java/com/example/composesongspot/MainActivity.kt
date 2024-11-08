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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.composesongspot.ui.theme.ComposeSongSpotTheme
import com.example.composesongspot.ui.theme.MainView

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        enableEdgeToEdge()
        setContent {
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

    private fun hideSystemUI() {
        //make the activity full screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView?.let { decorView ->
                decorView.windowInsetsController?.let { insetsController ->
                    insetsController.hide(WindowInsets.Type.systemBars())
                    insetsController.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }
}





