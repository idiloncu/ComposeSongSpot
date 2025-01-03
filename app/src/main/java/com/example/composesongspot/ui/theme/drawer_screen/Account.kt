package com.example.composesongspot.ui.theme.drawer_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.composesongspot.R
import com.example.composesongspot.ui.theme.ViewModel.SongViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun Account(viewModel: SongViewModel = hiltViewModel(), navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            name = Firebase.auth.currentUser?.displayName.toString(),
            navController = navController
        )
        Spacer(modifier = Modifier.height(4.dp))
        ProfileSection(modifier = Modifier.padding(horizontal = 20.dp))

        LazyColumnForUser(viewModel, currentUser = Firebase.auth.currentUser!!.uid)

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

                permissions[Manifest.permission.CAMERA] == true &&
                        permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true -> {
                    Log.d("Home", "Camera and storage access granted.")

                }
            }
        }

        LaunchedEffect(Unit) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
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
                    val musicList = songs.map { song ->
                        MusicCardInfoForUsers(
                            songName = song.songName,
                            artistName = song.artistName,
                            whoShared = song.whoShared,
                            userName = song.userName,
                            location = song.location.ifBlank { "Unknown Location" },
                            songUrl = song.songUrl,
                            albumCoverUrl = song.albumCoverUrl
                        )
                    }

                    LazyColumn {
                        items(musicList) { song ->
                            UserCardItems(item = song)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopAppBar(name: String, navController: NavController) {
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
        Spacer(modifier = Modifier.padding(start = 110.dp))
        Text(
            text = name,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

@Composable
fun ProfileSection(
    modifier: Modifier = Modifier
) {
    var imageUri by rememberSaveable { mutableStateOf("") }
    val painter = rememberImagePainter(
        if (imageUri.isEmpty())
            R.drawable.winking
        else
            imageUri
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it.toString() }

    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier
                .padding(8.dp)
                .size(100.dp)
        ) {
            rememberAsyncImagePainter(imageUri).let {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable { launcher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            }
        }
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Profile Picture",
            tint = Color.White,
            modifier = Modifier
                .offset(x = (-13).dp, y = 30.dp)
                .size(25.dp)
                .background(Color.DarkGray, CircleShape)
                .padding(4.dp)
                .clickable {
                    launcher.launch("image/*")
                }
        )
    }
}

@Composable
fun UserCardItems(item: MusicCardInfoForUsers) {
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    text = "Location: ${item.location.ifBlank { "Unknown Location" }}",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun LazyColumnForUser(viewModel: SongViewModel, currentUser: String) {
    var musicList by remember { mutableStateOf<List<MusicCardInfoForUsers>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getAllSongsForUser(
            currentUser = Firebase.auth.currentUser!!.uid,
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

                musicList = songInfoList.filter { it.whoShared == currentUser }
                    .map { songInfo ->
                        MusicCardInfoForUsers(
                            songName = songInfo.songName,
                            artistName = songInfo.artistName,
                            whoShared = songInfo.whoShared,
                            userName = songInfo.userName,
                            location = locationText,
                            songUrl = songInfo.songUrl,
                            albumCoverUrl = songInfo.albumCoverUrl
                        )
                    }
            },
            onFailure = { errorMessage ->
                Log.e("LazyColumnDemo", "Error fetching songs: $errorMessage")
            }
        )
    }

    LazyColumn {
        itemsIndexed(musicList) { _, item ->
            UserCardItems(item = item)
        }
    }
}

data class MusicCardInfoForUsers(
    val songName: String,
    val artistName: String,
    val whoShared: String,
    val userName: String,
    val location: String,
    val songUrl: String,
    val albumCoverUrl: String
)