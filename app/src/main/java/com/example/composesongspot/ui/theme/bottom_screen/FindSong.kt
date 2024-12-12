package com.example.composesongspot.ui.theme.bottom_screen

import android.app.Activity
import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var isRecording by rememberSaveable { mutableStateOf(false) }
    val application = context.applicationContext as Application
    val voiceToTextParser by lazy { VoiceToTextParser(application) }

    // Request audio permissions
    LaunchedEffect(Unit) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            0
        )
    }

    var canRecord by remember { mutableStateOf(false) }
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            canRecord = isGranted
        }
    )

    LaunchedEffect(key1 = recordAudioLauncher) {
        recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    val state by voiceToTextParser.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (state.isSpeaking) {
                    voiceToTextParser.stopListening()
                } else {
                    voiceToTextParser.startListening("")
                }
            }) {
                AnimatedContent(targetState = state.isSpeaking) {
                    if (it) {
                        Text(text = "Speaking")
                    } else {
                        Text(text = state.spokenTest.ifEmpty { "Click on mic to start recording" })
                    }
                }
            }
        }
    )
    { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally)
        {}
//        ) {
//            Button(onClick = {
//                File(context.cacheDir, "audio.mp3").also {
//                    recorder.start(it)
//                    audioFile = it
//                    isRecording = true
//                }
//            }) {
//                Text(text = "START")
//            }
//
//            Button(onClick = {
//                recorder.stop()
//                isRecording = false
//            }) {
//                Text(text = "STOP")
//            }
//
//            Text(
//                text = if (isRecording) "Recording..." else "Not Recording!",
//                color = if (isRecording) Color.Green else Color.Red
//            )
//
//            Button(onClick = {
//                player.playFile(audioFile ?: return@Button)
//            }) {
//                Text(text = "PLAY")
//            }
//
//            Button(onClick = {
//                player.stop()
//            }) {
//                Text(text = "STOP PLAYING")
//            }
//        }
    }
}