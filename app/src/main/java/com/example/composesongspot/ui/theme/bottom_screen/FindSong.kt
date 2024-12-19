package com.example.composesongspot.ui.theme.bottom_screen

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.example.composesongspot.BuildConfig.API_TOKEN
import com.example.composesongspot.ui.theme.ViewModel.SongViewModel
import com.example.composesongspot.ui.theme.bottom_screen.recorder.AndroidAudioRecorder
import com.example.composesongspot.ui.theme.network.Result
import com.example.composesongspot.ui.theme.network.SongResultResponse
import java.io.ByteArrayOutputStream
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
    var songResponse by remember { mutableStateOf<Result<SongResultResponse?>>(Result.loading()) }

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
        Spacer(modifier = Modifier.padding(8.dp))
        Button(onClick = {
            if (isRecording) {
                recorder.stop()
                isRecording = false
                isLoading = true
                if (audioFile == null || !audioFile!!.exists()) {
                    Toast.makeText(
                        context,
                        "Audio file is null or does not exist",
                        Toast.LENGTH_LONG
                    ).show()
                }
                audioFile?.let { file ->
                    viewModel.uploadMp3(file,
                        onSuccess = { downloadUrl ->
                            viewModel.searchSong(
                                apiToken = API_TOKEN,
                                url = downloadUrl,
                                _return = "spotify",
                            ) { result ->
                                when (result) {
                                    is Result.Loading -> songInfo = "Loading..."
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
        Spacer(modifier = Modifier.padding(20.dp))
        if (isLoading) {
            LoadingAnimationDots()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        LaunchedEffect(viewModel.searchSongResponse.value) {
            println(viewModel.searchSongResponse.value)
            viewModel.searchSongResponse.value?.let {
                songResponse = it
            }
        }

        if (songResponse is Result.Success) {
            (songResponse as? Result.Success<SongResultResponse?>)?.data?.result?.let {
                Text(text = "Singer: ${it.artist}", color = Color.Black)

                Text(text = "Title: ${it.title}", color = Color.Black)

                Text(text = "Album: ${it.album}", color = Color.Black)

                Text(text = "Release Date: ${it.release_date}", color = Color.Black)

                Text(text = "Label: ${it.label}", color = Color.Black)

                Text(text = "Time Code: ${it.timecode}", color = Color.Black)

                Text(text = "Song Link: ${it.song_link}", color = Color.Black)

                it.spotify.album.images.firstOrNull()?.let { songImageUrl ->
                    AsyncImage(
                        model = songImageUrl.url,
                        contentDescription = "Song Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

suspend fun getImageBytesFromUrl(context: Context, imageUrl: String): ByteArray {
    val loader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false) // Gerekirse, çünkü ByteArray'e dönüştüreceğiz
        .build()
    val result = (loader.execute(request) as? ErrorResult)?.drawable
    val bitmap = (result as? BitmapDrawable)?.bitmap

    // Bitmap'i ByteArray'e dönüştürme
    val outputStream = ByteArrayOutputStream()
    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}