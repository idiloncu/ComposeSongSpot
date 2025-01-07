package com.example.composesongspot.ui.theme.network

data class ResultX(
    val album: String? = "Unknown Album",
    val artist: String? = "Unknown Artist",
    val label: String? = "Unknown Label",
    val release_date: String? = "Unknown Date",
    val song_link: String = "Unknown Link",
    val spotify: Spotify?,
    val timecode: String? = "Unknown Timecode",
    val title: String? = "Unknown Song Name"
)