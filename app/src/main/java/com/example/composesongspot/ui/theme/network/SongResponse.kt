package com.example.composesongspot.ui.theme.network

data class SongResponse(
val status: String? = "Unknown Status",
val result: Information?
)

data class Information(
    val artist: String? = "Unknown Artist",
    val title: String?="Unknown Song",
    val album: String?="Unknown Album",
    val release_date: String?="Unknown Date",
    val label: String?="Unknown Label",
    val timecode: String?="Unknown Timecode",
    val song_link: String?="Unknown Link"
)

//title = song name
//artist = artist name