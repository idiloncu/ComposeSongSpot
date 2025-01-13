package com.example.composesongspot.ui.theme.bottom_screen

import androidx.compose.foundation.layout.Column
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun Comment(navController: NavController){
    Column {
        Text(text = "FOLLOWERS COMMENTS")
    }

}