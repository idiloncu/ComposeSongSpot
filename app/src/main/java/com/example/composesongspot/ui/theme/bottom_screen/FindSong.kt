package com.example.composesongspot.ui.theme.bottom_screen

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    var isLoading by remember { mutableStateOf(false) }

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
            } else if (isRecording) {
                isLoading = true
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
                isLoading = true
                if (audioFile == null || !audioFile!!.exists()) {
                    Toast.makeText(context, "Audio file is null or does not exist", Toast.LENGTH_LONG).show()
                }
                audioFile?.let { file ->
                    viewModel.uploadMp3(file,
                        onSuccess = { downloadUrl ->
                            viewModel.searchSong(
                                apiToken = API_TOKEN,
                                url = downloadUrl
                            ) { result ->
                                when (result) {
                                    is Result.Loading -> songInfo = mapOf("Status" to "Loading Song Informations").toString()
                                    is Result.Success -> {
                                        result.data?.result?.let {
                                            songInfo = mapOf(
                                                "Song Title" to it.title,
                                                "Artist" to it.artist,
                                                "Album" to it.album,
                                                "Release Date" to it.release_date,
                                                "Label" to it.label,
                                                "Time Code" to it.timecode,
                                                "Song Link" to it.song_link
                                            ).toString()
                                        }

//                                        songInfo = "SONG NAME: $songTitle" +
//                                                " \n ARTIST: $artistName \n" +
//                                                "ALBUM: $albumName \n" +
//                                                "RELEASE DATE: $releaseDate" +
//                                                "LABEL: $label" +
//                                                "TIME CODE: $timeCode" +
//                                                "SONG LINK: $songLink"
                                        isLoading = false
                                    }

                                    is Result.Error -> {
                                        songInfo = mapOf("Error" to result.message).toString()
                                        isLoading = false
                                        Log.d("FINDSONG", "${result.message}")
                                    }
                                }
                            }
                        },
                        onFailure = { errorMessage ->
                            Log.d("KONTROLL", "Upload Failure: $errorMessage")
                            songInfo = mapOf("Upload Error" to errorMessage).toString()
                            isLoading = false
                        }
                    )
                }
            }
        }) {
            Text(text = "STOP")
        }
        Spacer(modifier = Modifier.padding(16.dp))
        if (isLoading) {
            LoadingAnimationDots()
        }
//        if (isRecording){
//            LoadingAnimationSpinner()
//        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        songInfo?.let { artist ->
            Text(text = "Singer: $artist", color = Color.Red)
        }
        songInfo?.let { title ->
            Text(text = "Title: $title", color = Color.Red)
        }
        songInfo?.let { album ->
            Text(text = "Album: $album", color = Color.Red)
        }
        songInfo?.let { release_date ->
            Text(text = "Release Date: $release_date", color = Color.Red)
        }
        songInfo?.let { label ->
            Text(text = "Label: $label", color = Color.Red)
        }
        songInfo?.let { time_code ->
            Text(text = "Time Code: $time_code", color = Color.Red)
        }
        songInfo?.let { song_link ->
            Text(text = "Song Link: $song_link", color = Color.Red)
        }
    }
}