package com.example.composesongspot.ui.theme.bottom_screen

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composesongspot.BuildConfig.API_TOKEN
import com.example.composesongspot.ui.theme.ViewModel.SongViewModel
import com.example.composesongspot.ui.theme.bottom_screen.recorder.AndroidAudioRecorder
import com.example.composesongspot.ui.theme.network.Result
import java.io.File

@Composable
fun FindSong(viewModel: SongViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as Activity
    val recorder = remember { AndroidAudioRecorder(context) }
    var audioFile: File? = null
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var songInfo by remember { mutableStateOf<String?>(null) }

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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            if (canRecord) {
                File(context.cacheDir, "audio.mp3").also {
                    recorder.start(it)
                    audioFile = it
                    isRecording = true
                }
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_LONG).show()
            }

        }) {
            Text(text = if (isRecording) "RECORDING" else "START")
        }
        Button(onClick = {
            if (isRecording) {
                recorder.stop()
                isRecording = false
                audioFile?.let { file ->
                    viewModel.uploadMp3(file,
                        onSuccess = { downloadUrl ->
                            // Burada searchSong fonksiyonunu çağırın
                            viewModel.searchSong(API_TOKEN, url = downloadUrl) { result ->
                                when (result) {
                                    is Result.Loading -> songInfo = "Şarkı bilgileri yükleniyor..."
                                    is Result.Success -> {
                                        val songTitle = result.data.information.title
                                        val artistName = result.data.information.artist
                                        songInfo = "Şarkı: $songTitle\nSanatçı: $artistName"
                                        Log.d("KONTROLL", "downloadUrl: $downloadUrl")
                                        Log.d("KONTROLL", "songTitle: $songTitle")
                                        Log.d("KONTROLL", "artistName: $artistName")
                                    }

                                    is Result.Error -> songInfo = "Hata: ${result.message}"
                                }
                            }
                        },
                        onFailure = { errorMessage ->
                            songInfo = "Yükleme hatası: $errorMessage"
                        }
                    )
                }
            }
        }) {
            Text(text = "STOP")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        songInfo?.let { info ->
            Text(text = info)
        }
        songInfo?.let { artist ->
            Text(text = "Singer: $artist")
        }
    }
}