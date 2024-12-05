package com.example.composesongspot.ui.theme.bottom_screen.recorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()

}