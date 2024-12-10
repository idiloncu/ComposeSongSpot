package com.example.composesongspot.ui.theme.bottom_sheet_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.composesongspot.R

@Composable
fun About(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SongSpot",
            style = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.about_part1),
            style = MaterialTheme.typography.subtitle1.copy(
                fontStyle = FontStyle.Normal,
                color = Color.DarkGray
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.about_part2) +
                    stringResource(R.string.about_part3),
            style = MaterialTheme.typography.body1,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.about_part4),
            style = MaterialTheme.typography.body1,
            lineHeight = 24.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Text(
            text = stringResource(R.string.about_part5),
            style = MaterialTheme.typography.h6.copy(
                fontStyle = FontStyle.Italic,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        )
    }
}