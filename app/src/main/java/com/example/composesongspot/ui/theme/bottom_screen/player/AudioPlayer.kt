package com.example.composesongspot.ui.theme.bottom_screen.player

import java.io.File

interface AudioPlayer {
    fun playFile(file: File)
    fun stop()
}