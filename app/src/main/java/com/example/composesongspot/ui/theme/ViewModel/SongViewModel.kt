package com.example.composesongspot.ui.theme.ViewModel

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composesongspot.BuildConfig.API_TOKEN
import com.example.composesongspot.ui.theme.bottom_screen.MusicCardInfo
import com.example.composesongspot.ui.theme.network.Result
import com.example.composesongspot.ui.theme.network.RetrofitInstance
import com.example.composesongspot.ui.theme.network.SongResultResponse
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val _searchSongResponse = MutableStateFlow<Result<SongResultResponse>?>(null)
    val searchSongResponse: StateFlow<Result<SongResultResponse>?> = _searchSongResponse
    private val _songs = mutableStateOf<List<MusicCardInfo>>(emptyList())
    val songs: State<List<MusicCardInfo>> = _songs
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun searchSong(
        apiToken: String,
        url: String,
        _return: String,
        callback: (Result<SongResultResponse>) -> Unit
    ) {
        viewModelScope.launch {
            callback(Result.Loading)
            try {
                val response =
                    RetrofitInstance.api.searchSong(apiToken, url, _return).enqueue(object :
                        Callback<SongResultResponse> {
                        override fun onResponse(
                            call: Call<SongResultResponse>,
                            response: Response<SongResultResponse>
                        ) {
                            response.body()?.let {
                                _searchSongResponse.value = Result.Success(it)
                                callback(Result.Success(it))
                            } ?: kotlin.run {
                                callback(Result.Error("Music not found"))
                            }
                            println(response.message())
                        }

                        override fun onFailure(call: Call<SongResultResponse>, t: Throwable) {
                            println("FAIL ${t.message}")
                        }
                    })
            } catch (e: Exception) {
                callback(Result.Error(e.message ?: "Unrecognized Error!"))
            }
        }
    }

    // upload an MP3 file to Firebase Storage
    fun uploadMp3(file: File, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val uuid = UUID.randomUUID()
        val mp3Name = "$uuid.mp3"
        val storage = Firebase.storage
        val reference = storage.reference
        val audioReference = reference.child("audios").child(mp3Name)
        audioReference.putFile(file.toUri()).addOnSuccessListener { taskSnapshot ->

            val audioReference = storage.reference.child("audios").child(mp3Name)
            audioReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                onSuccess(downloadUrl)
                println(downloadUrl)

                val postMap = hashMapOf<String, Any>()
                postMap.put("downloadUrl", downloadUrl)
            }
        }
    }

    // upload an MP3 file URL's to server
    fun postAudioToServer(downloadUrl: String) {
        _searchSongResponse.value = Result.Loading
        val apiInterface = RetrofitInstance.api
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("url", downloadUrl)
            .addFormDataPart("api_token", API_TOKEN)
            .addFormDataPart("return", "spotify")
            .build()

        apiInterface.somePostMethod(requestBody).enqueue(
            object : Callback<SongResultResponse> {
                override fun onResponse(
                    call: Call<SongResultResponse>,
                    response: Response<SongResultResponse>
                ) {
                    Log.d("Raw Response", "onResponse: ${response.body()}")

                    if (response.isSuccessful) {
                        Log.d("ResponseData", "Received: ${response.body()}")
                        _searchSongResponse.value = Result.Success(response.body()!!)
                    } else {
                        Log.e("ResponseError", "Error: ${response.message()}")
                        _searchSongResponse.value =
                            Result.Error("Server Side Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<SongResultResponse>, t: Throwable) {
                    _searchSongResponse.value = Result.Error("Connection Error: ${t.message}")
                }
            })
    }

    fun getAllSongs(onSuccess: (List<MusicCardInfo>) -> Unit, onFailure: (String) -> Unit) {
        val uuid = UUID.randomUUID()
        val mp3Name = "$uuid.mp3"
        val storage = Firebase.storage
        val reference = storage.reference.child("audios").child(mp3Name)
        val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(application)
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                application,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onFailure("Location permission not granted.")
            return
        }

        try {
            fusedLocationProvider.lastLocation.addOnSuccessListener { location ->
                val userLocation = if (location != null) {
                    "Lat: ${location.latitude}, Lng: ${location.longitude}"
                } else {
                    "Unknown Location"
                }

                reference.listAll().addOnSuccessListener { listResult ->
                    val songInfoList = mutableListOf<MusicCardInfo>()
                    val totalItems = listResult.items.size

                    if (totalItems == 0) {
                        onSuccess(emptyList())
                        return@addOnSuccessListener
                    }

                    listResult.items.forEach { item ->
                        item.getMetadata().addOnSuccessListener { metadata ->
                            val songName = metadata.getCustomMetadata("songName") ?: item.name
                            val artistName =
                                metadata.getCustomMetadata("artistName") ?: "Unknown Artist"
                            val albumName =
                                metadata.getCustomMetadata("albumName") ?: "Unknown Album"
                            val whoShared = metadata.getCustomMetadata("whoShared")
                                ?: Firebase.auth.currentUser?.displayName ?: ""

                            item.downloadUrl.addOnSuccessListener { uri ->
                                val songInfo = MusicCardInfo(
                                    songName = songName,
                                    artistName = artistName,
                                    albumName = albumName,
                                    whoShared = whoShared,
                                    location = userLocation,
                                    songUrl = uri.toString()
                                )
                                songInfoList.add(songInfo)

                                if (songInfoList.size == totalItems) {
                                    onSuccess(songInfoList)
                                }
                            }.addOnFailureListener { exception ->
                                onFailure("Failed to get download URL for: ${item.name}, Error: ${exception.message}")
                            }
                        }.addOnFailureListener { exception ->
                            onFailure("Failed to get metadata for: ${item.name}, Error: ${exception.message}")
                        }
                    }
                }.addOnFailureListener { exception ->
                    onFailure("Failed to list all songs: ${exception.message}")
                }
            }
        } catch (e: Exception) {
            onFailure("Permission error: ${e.message}")
        }
    }

    fun saveSongToDatabase(
        song: MusicCardInfo,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val database = Firebase.database
        val songRef = database.reference.child("songs").push()

        val songData = mapOf(
            "songName" to song.songName,
            "artistName" to song.artistName,
            "albumName" to song.albumName,
            "whoShared" to song.whoShared,
            "location" to song.location,
            "songUrl" to song.songUrl,
            "timestamp" to System.currentTimeMillis()
        )

        songRef.setValue(songData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure("Failed to save song to Realtime Database: ${exception.message}")
            }
    }

    fun saveSearchedSong(
        apiToken: String,
        url: String,
        _return: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        searchSong(apiToken, url, _return) { result ->
            when (result) {
                is Result.Success -> {
                    val songResponse = result.data
                    val songInfo = MusicCardInfo(
                        songName = songResponse.result.title ?: "Unknown Song",
                        artistName = songResponse.result.artist ?: "Unknown Artist",
                        albumName = songResponse.result.album ?: "Unknown Album",
                        whoShared = "CurrentUser",
                        location = "Unknown Location",
                        songUrl = url
                    )

                    saveSongToDatabase(
                        song = songInfo,
                        onSuccess = {
                            onSuccess()
                        },
                        onFailure = { error ->
                            onFailure("Failed to save searched song: $error")
                        }
                    )
                }

                is Result.Error -> {
                    onFailure("Failed to search song: ${result.message}")
                }

                is Result.Loading -> {

                }
            }
        }
        saveSearchedSong(
            apiToken = "your_api_token",
            url = "song_url",
            _return = "spotify",
            onSuccess = {
                println("Song saved successfully!")
            },
            onFailure = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun fetchSongs() {
        getAllSongs(
            onSuccess = { songList ->
                _songs.value = songList
            },
            onFailure = { error ->
                _errorMessage.value = error
            }
        )
    }
}
