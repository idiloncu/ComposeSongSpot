package com.example.composesongspot.ui.theme.bottom_screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.composesongspot.BuildConfig.API_TOKEN
import com.example.composesongspot.R
import com.example.composesongspot.ui.theme.ViewModel.SongViewModel
import com.example.composesongspot.ui.theme.bottom_screen.recorder.AndroidAudioRecorder
import com.example.composesongspot.ui.theme.network.Result
import com.example.composesongspot.ui.theme.network.SongResultResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import java.io.File


@Composable
fun FindSong(viewModel: SongViewModel = hiltViewModel(), navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val recorder = remember { AndroidAudioRecorder(context) }
    var audioFile: File? = null
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var songInfo by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val songResponse by viewModel.searchSongResponse.collectAsState()
    val TAG = "FindSong"
    val second = remember { mutableStateOf(10) } // Geri sayım başlangıç değeri
    var isStopButtonEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (second.value > 0) {
            delay(1000L)
            second.value -= 1
        }
    }

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
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 250.dp, start = 75.dp, end = 75.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        if (isLoading) {
            LoadingAnimationDots()
            Text(
                text = "SEARCHING THE SONG",
                style = TextStyle(fontWeight = FontWeight.Bold, color = Color.Blue),
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.padding(top = 150.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (canRecord) {
                        File(context.cacheDir, "audio.mp3").also {
                            recorder.start(it)
                            audioFile = it
                            isRecording = true
                            second.value = 10
                            isStopButtonEnabled = false
                        }
                    } else if (isRecording) {
                        isLoading = true
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.permission_denied), Toast.LENGTH_LONG
                        ).show()
                    }
                },
            ) {
                Text(
                    text = if (isRecording) stringResource(R.string.recording) else stringResource(
                        R.string.start
                    )
                )
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
                            context.getString(R.string.audio_file_is_null_or_does_not_exist),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    audioFile?.let { file ->
                        viewModel.uploadMp3(file,
                            onSuccess = { downloadUrl ->
                                Log.d(TAG, "FindSong: $downloadUrl")
                                Log.d("FindSong", "FindSong: ${file.name}")
                                viewModel.searchSong(
                                    apiToken = API_TOKEN,
                                    url = downloadUrl,
                                    _return = "spotify",
                                ) { result ->
                                    when (result) {
                                        is Result.Loading -> songInfo =
                                            context.getString(R.string.loading)

                                        is Result.Success -> {
                                            result?.data?.result?.let {
                                                songInfo = mapOf(
                                                    context.getString(R.string.song_title) to it.title?.ifBlank { "Unknown Song Name" },
                                                    context.getString(R.string.artist) to it.artist?.ifBlank { "Unknown Artist" },
                                                    context.getString(R.string.album) to it.album?.ifBlank { "Unknown Album" },
                                                    context.getString(R.string.release_date) to it.release_date?.ifBlank { "Unknown Release Date" },
                                                    context.getString(R.string.label) to it.label?.ifBlank { "Unknown Label" },
                                                    context.getString(R.string.time_code) to it.timecode?.ifBlank { "Unknown Time Code" },
                                                    context.getString(R.string.song_link) to it.song_link?.ifBlank { "Unknown Link" }
                                                ).toString()

                                                Log.d(
                                                    TAG,
                                                    "searchSong-title: ${result.data.result.title}"
                                                )
                                                Log.d(
                                                    TAG,
                                                    "searchSong-artist: ${result.data.result.artist}"
                                                )
                                                Log.d(TAG, "searchSong-downloadUrl: $downloadUrl")

                                                val musicCardInfo = it.title?.let { it1 ->
                                                    it.artist?.let { it2 ->
                                                        it.album?.let { it3 ->
                                                            MusicCardInfo(
                                                                songName = it1.ifBlank { "Unknown Song Name" },
                                                                artistName = it2.ifBlank { "Unknown Artist" },
                                                                albumName = it3.ifBlank { "Unknown Album" },
                                                                whoShared = Firebase.auth.currentUser?.uid
                                                                    ?: "",
                                                                userName = Firebase.auth.currentUser?.displayName
                                                                    ?: "Unknown",
                                                                location = "",
                                                                songUrl = it.song_link,
                                                                albumCoverUrl = it.spotify?.album?.images?.firstOrNull()?.url
                                                                    ?: ""
                                                            )
                                                        }
                                                    }
                                                }
                                                musicCardInfo?.let { it1 ->
                                                    viewModel.saveSongToDatabase(
                                                        it1,
                                                        onSuccess = {
                                                            Log.d(
                                                                "FindSong",
                                                                "saveSongToDatabase: Success"
                                                            )
                                                        },
                                                        onFailure = {
                                                            Log.d(
                                                                "FindSong",
                                                                "saveSongToDatabase: Failed $it"
                                                            )
                                                        })
                                                }
                                            }

                                            isLoading = false
                                        }

                                        is Result.Error -> {
                                            songInfo = mapOf("Error" to result.message).toString()
                                            isLoading = false
                                            Toast.makeText(context,"SOUND DOES NOT EXIST" , Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            onFailure = { errorMessage ->
                                songInfo = mapOf("Upload Error" to errorMessage).toString()
                                isLoading = false
                            }
                        )
                    }
                }
            }) {
                Text(text = stringResource(R.string.stop))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        if (songResponse is Result.Success) {
            (songResponse as? Result.Success<SongResultResponse?>)?.data?.result?.let {
                Text(
                    text = stringResource(R.string.singer, it.artist ?: "Unknown Artist"),
                    color = Color.Black
                )

                Text(
                    text = stringResource(R.string.title, it.title ?: "Unknown Song Name"),
                    color = Color.Black
                )

                Text(
                    text = stringResource(R.string.album_text, it.album ?: "Unknown Album"),
                    color = Color.Black
                )

                Text(
                    text = stringResource(
                        R.string.release_date_text,
                        it.release_date ?: "Unknown Release Date"
                    ),
                    color = Color.Black
                )
                Text(
                    text = stringResource(R.string.label_text, it.label ?: "Unknown Label"),
                    color = Color.Black
                )

                Text(
                    text = stringResource(
                        R.string.time_code_text,
                        it.timecode ?: "Unknown Time Code"
                    ),
                    color = Color.Black
                )
                ClickableText(text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Blue, fontSize = 16.sp)) {
                        append((stringResource(id = R.string.song_link)))
                    }
                    pushStringAnnotation(tag = "URL", annotation = it.song_link)
                    withStyle(
                        style = SpanStyle(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 16.sp
                        )
                    ) {
                        append(it.song_link)
                    }
                    pop()
                }, onClick = { offset ->
                    val annotations = buildAnnotatedString {
                        pushStringAnnotation(tag = "URL", annotation = it.song_link)
                    }.getStringAnnotations(tag = "URL", start = offset, end = offset)

                    annotations.firstOrNull()?.let { annotation ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        context.startActivity(intent)
                    }
                })

                it.spotify?.album?.images?.firstOrNull()?.let { songImageUrl ->
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

@Composable
fun TopAppBar(navController: NavController) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Spacer(modifier = Modifier.padding(bottom = 40.dp, top = 20.dp))

        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .size(44.dp)
                .padding(start = 10.dp)
                .clickable {
                    navController.navigate("Home")
                }
        )
    }
}


