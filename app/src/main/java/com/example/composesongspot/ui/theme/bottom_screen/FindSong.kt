package com.example.composesongspot.ui.theme.bottom_screen

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.composesongspot.ui.theme.bottom_screen.player.AndroidAudioPlayer
import com.example.composesongspot.ui.theme.bottom_screen.recorder.AndroidAudioRecorder
import java.io.File

@Composable
fun FindSong() {
    val context = LocalContext.current
    val activity = context as Activity
    val recorder = remember { AndroidAudioRecorder(context) }
    val player = remember { AndroidAudioPlayer(context) }
    var audioFile: File? = null
    activity.let {
        ActivityCompat.requestPermissions(
            it,
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            0
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            File(context.cacheDir, "audio.mp3").also {
                recorder.start(it)
                audioFile = it

            }
        }) {
            Text(text = "START")
        }
        Button(onClick = {
            recorder.stop()
        }) {
            Text(text = "STOP")
        }

        Button(onClick = {
            player.playFile(audioFile ?: return@Button)
        }) {
            Text(text = "PLAY")
        }
        Button(onClick = {
            player.stop()
        }) {
            Text(text = "STOP PLAYING")
        }
    }
}