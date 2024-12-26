package com.example.composesongspot.ui.theme.network

data class ResultX(
    val album: String,
    val artist: String,
    val label: String,
    val release_date: String,
    val song_link: String,
    val spotify: Spotify,
    val timecode: String,
    val title: String? = "Unknown Song Name"
)