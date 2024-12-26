package com.example.composesongspot.ui.theme.ViewModel

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composesongspot.BuildConfig.API_TOKEN
import com.example.composesongspot.R
import com.example.composesongspot.ui.theme.bottom_screen.MusicCardInfo
import com.example.composesongspot.ui.theme.network.Result
import com.example.composesongspot.ui.theme.network.RetrofitInstance
import com.example.composesongspot.ui.theme.network.SongResultResponse
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor() : ViewModel() {
    var currentLocation by mutableStateOf<Location?>(null)
        private set
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
        val db = com.google.firebase.Firebase.database
        val reference = db.reference.child("songs")

        try {
            reference.get()
                .addOnSuccessListener { listResult ->
                    Log.d("SongViewModel", "getAllSongs: ${listResult.value}")
                    val songsList = mutableListOf<MusicCardInfo>()
                    listResult.children.forEach { item ->
                        val songValueMap = item.value as? Map<*, *>
                        val songInfo = MusicCardInfo(
                            songName = songValueMap?.get("songName").toString(),
                            artistName = songValueMap?.get("artistName").toString(),
                            albumName = songValueMap?.get("albumName").toString(),
                            whoShared = songValueMap?.get("whoShared").toString(),
                            userName = songValueMap?.get("userName").toString(),
                            location = songValueMap?.get("location").toString(),
                            songUrl = songValueMap?.get("songUrl").toString(),
                            albumCoverUrl = songValueMap?.get("albumCoverUrl").toString()
                        )

                        songsList.add(songInfo)
                    }
                    onSuccess(songsList)
                }.addOnFailureListener { exception ->
                    onFailure("Failed to list all songs: ${exception.message}")
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
            "timestamp" to System.currentTimeMillis(),
            "albumCoverUrl" to song.albumCoverUrl
        )

        songRef.setValue(songData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure("Failed to save song to Realtime Database: ${exception.message}")
            }
    }

    private fun fetchLocation(context: Context, onSuccess: (Location?) -> Unit, onFailure: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                onSuccess(location)
            }.addOnFailureListener { exception ->
                onFailure(exception.message ?: "Failed to get location")
            }
        } catch (e: SecurityException) {
            onFailure("Permission denied: ${e.message}")
        }
    }

    fun getHumanReadableLocation(context: Context, latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                "${address.locality ?: context.getString(R.string.bilinmeyen_sehir)}, ${address.countryName ?: context.getString(
                    R.string.bilinmeyen_ulke
                )}"
            } else {
                 "Konum bilgisi bulunamadı"
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Hata: ${e.message}")
            "Geocoder kullanılırken hata oluştu"
        }
    }

    fun updateLocation(context: Context) {
        fetchLocation(context,
            onSuccess = { location ->
                currentLocation = location
                if (location != null) {
                    val humanReadableLocation = getHumanReadableLocation(
                        context, location.latitude, location.longitude
                    )
                    Log.d("SongViewModel", "Location updated: $humanReadableLocation")
                } else {
                    Log.d("SongViewModel", "Konum bilgisi alınamadı")
                }
            },
            onFailure = { error ->
                Log.e("SongViewModel", "Error fetching location: $error")
            }
        )
    }

    inline fun <reified T> String.convertToListObject(): List<T>? {
        val listType: Type = object : TypeToken<List<T?>?>() {}.type
        return Gson().fromJson<List<T>>(this, listType)
    }
}
