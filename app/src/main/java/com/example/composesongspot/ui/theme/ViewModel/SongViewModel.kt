package com.example.composesongspot.ui.theme.ViewModel

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composesongspot.BuildConfig.API_TOKEN
import com.example.composesongspot.ui.theme.network.Result
import com.example.composesongspot.ui.theme.network.RetrofitInstance
import com.example.composesongspot.ui.theme.network.SongResultResponse
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
class SongViewModel @Inject constructor() : ViewModel() {

    private val _searchSongResponse = MutableStateFlow<Result<SongResultResponse>?>(null)
    val searchSongResponse: StateFlow<Result<SongResultResponse>?> = _searchSongResponse

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
                            Log.d("eT", "onResponse: $response")
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
                Log.d("AUDIO_URL", "uploadMp3: $downloadUrl")

                val postMap = hashMapOf<String, Any>()
                postMap.put("downloadUrl", downloadUrl)
                Log.d("AUDIO_URL", "downloadUrl: $downloadUrl")
            }
        }
    }

    fun uploadAlbumPicture(file: File, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val uuid = UUID.randomUUID()
        val picture = "$uuid.png"
        val storage = Firebase.storage
        val reference = storage.reference
        val audioReference = reference.child("album_images").child(picture)
        audioReference.putFile(file.toUri()).addOnSuccessListener { taskSnapshot ->

            val audioReference = storage.reference.child("audios").child(picture)
            audioReference.downloadUrl.addOnSuccessListener { uri ->
                val pictureUrl = uri.toString()
                onSuccess(pictureUrl)
                println(pictureUrl)
                Log.d("PICTURE_URL", "uploadMp3: $pictureUrl")

                val postMap = hashMapOf<String, Any>()
                postMap.put("downloadUrl", pictureUrl)
                Log.d("PICTURE_URL", "downloadUrl: $pictureUrl")
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

    fun uploadMp3AndPostToServer(file: File) {
        uploadMp3(file,
            onSuccess = { downloadUrl: String ->
                postAudioToServer(downloadUrl)
                Log.d("SONGWW", "uploadMp3AndPostToServer:$downloadUrl ")
            },
            onFailure = { errorMessage: String ->
                _searchSongResponse.value = Result.Error("File Upload Error: $errorMessage")
            })
    }
}
