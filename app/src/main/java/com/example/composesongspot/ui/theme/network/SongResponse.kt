package com.example.composesongspot.ui.theme.network

import java.io.Serializable

data class SongResponse(
val status: String,
val information: Information
):Serializable

data class Information(
    val artist: String,
    val title: String,
    val album: String,
    val song_link: String
):Serializable

//title = song name
//artist = artist name