package com.example.composesongspot.ui.theme

import com.example.composesongspot.R

data class MusicCardInfo(
    var location: String,
    var songName : String,
    var artistName : String,
    var albumName : String,
    var whoShared : String,
    //when you use api change image type to String
    var albumPhoto : Int,
)

fun getAllMusicCardInfo():List<MusicCardInfo>{
    return listOf<MusicCardInfo>(
        MusicCardInfo("U.S.A", "Blank Space", "Taylor Swift", "Reputation", "İdil", R.drawable.album1),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album2),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album3),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album4),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album5),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album6),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album7),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album8),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album9),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album10),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album11),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album12),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album13),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album14),
        MusicCardInfo("location", "songName", "artistName", "albumName", "whoShared", R.drawable.album15)
    )
}