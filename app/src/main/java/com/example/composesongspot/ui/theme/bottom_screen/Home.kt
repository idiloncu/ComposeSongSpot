package com.example.composesongspot.ui.theme.bottom_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Card
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.composesongspot.ui.theme.ViewModel.SongViewModel

@Composable
fun Home(navController: NavController, viewModel: SongViewModel = hiltViewModel()) {
    LazyColumnDemo(navController, viewModel)
    val songs by viewModel.songs
    val context = LocalContext.current
    val errorMessage by viewModel.errorMessage
    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                Log.d("Home", "Precise location access granted.")
            }

            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                Log.d("Home", "Approximate location access granted.")
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.updateLocation(context)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            errorMessage != null -> {
                Text(
                    text = "Error: ${errorMessage.orEmpty()}",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                LazyColumn {
                    items(songs) { song ->
                        CardItems(item = song)
                    }
                }
            }
        }
    }
}

@Composable
fun LazyColumnDemo(navController: NavController, viewModel: SongViewModel) {
    var musicList by remember { mutableStateOf<List<MusicCardInfo>>(emptyList()) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.getAllSongs(
            onSuccess = { songInfoList ->
                val currentLocation = viewModel.currentLocation
                val locationText = if (currentLocation != null) {
                    viewModel.getHumanReadableLocation(
                        context = context,
                        latitude = currentLocation.latitude,
                        longitude = currentLocation.longitude
                    )
                } else {
                    "No Location"
                }
                Log.d("HOME", "currentLocation: $currentLocation")
                musicList = songInfoList.map { songInfo ->
                    MusicCardInfo(
                        songName = songInfo.songName,
                        artistName = songInfo.artistName,
                        albumName = songInfo.albumName,
                        whoShared = songInfo.whoShared,
                        userName = songInfo.userName,
                        location = locationText,
                        songUrl = songInfo.songUrl,
                        albumCoverUrl = songInfo.albumCoverUrl
                    )
                }
                println(musicList)
                Log.d("HOME", "locationText: $locationText ")
            },
            onFailure = { errorMessage ->
                Log.e("LazyColumnDemo", "Error fetching songs: $errorMessage")
            }
        )
    }

    LazyColumn(content = {
        itemsIndexed(musicList) { _, item ->
            CardItems(item = item)
        }
    })
}

@Composable
fun CardItems(item: MusicCardInfo) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.songUrl))
                context.startActivity(intent)
            },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            AsyncImage(
                model = item.albumCoverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(26.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.songName,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Artist: ${item.artistName}",
                    style = MaterialTheme.typography.body1,
                    color = Color.DarkGray
                )
                Text(
                    text = "Album: ${item.albumName}",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
                Text(
                    text = "Shared by: ${item.whoShared.ifBlank { "Unknown User" }}",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
                Text(
                    text = "Location: ${item.location.ifBlank { "Unknown Location" }}",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }
        }
    }
}

data class MusicCardInfo(
    val songName: String,
    val artistName: String,
    val albumName: String,
    val whoShared: String,
    val userName: String,
    val location: String,
    val songUrl: String,
    val albumCoverUrl: String
)