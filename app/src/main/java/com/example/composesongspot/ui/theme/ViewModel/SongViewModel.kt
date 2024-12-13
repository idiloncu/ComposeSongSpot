package com.example.composesongspot.ui.theme.ViewModel

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composesongspot.ui.theme.network.Result
import com.example.composesongspot.ui.theme.network.RetrofitInstance
import com.example.composesongspot.ui.theme.network.SongResponse
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.UUID

class SongViewModel : ViewModel() {

    private val _searchSongResponse = MutableStateFlow<Result<SongResponse>?>(null)

    fun searchSong(apiToken: String, url: String, callback: (Result<SongResponse>) -> Unit) {
        viewModelScope.launch {
            callback(Result.Loading)
            try {
                val response = RetrofitInstance.api.searchSong(apiToken, url).execute()
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(Result.Success(it))
                    } ?: callback(Result.Error("Empty Response Received"))
                } else {
                    callback(Result.Error("Callback Error: ${response.message()}"))
                }
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
            }
        }
    }

    // upload an MP3 file URL's to server
    fun postAudioToServer(downloadUrl: String) {
        _searchSongResponse.value = Result.Loading
        val apiInterface = RetrofitInstance.api
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("downloadUrl", downloadUrl)
            .build()

        apiInterface.somePostMethod(requestBody).enqueue(
            object : Callback<SongResponse> {
                override fun onResponse(
                    call: Call<SongResponse>,
                    response: Response<SongResponse>
                ) {
                    if (response.isSuccessful) {
                        _searchSongResponse.value = Result.Success(response.body()!!)
                    } else {
                        _searchSongResponse.value =
                            Result.Error("Server Side Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<SongResponse>, t: Throwable) {
                    _searchSongResponse.value = Result.Error("Connection Error: ${t.message}")
                }
            })
    }

    fun uploadMp3AndPostToServer(file: File) {
        uploadMp3( file,
            onSuccess = { downloadUrl:String ->
                postAudioToServer(downloadUrl)
            },
            onFailure = { errorMessage:String ->
                _searchSongResponse.value = Result.Error("File Upload Error: $errorMessage")
            })
    }
}
