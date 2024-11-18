package com.example.composesongspot.ui.theme.bottomSc

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.composesongspot.Screen
import com.example.composesongspot.ui.theme.MusicCardInfo
import com.example.composesongspot.ui.theme.getAllMusicCardInfo

@Composable
fun Home(navController: NavController) {
    LazyColumnDemo(navController)
}

@Composable
fun LazyColumnDemo(navController: NavController) {
    val myList = getAllMusicCardInfo()
    LazyColumn(content = {
        itemsIndexed(myList, itemContent = { index, item ->
            CardItems(item = item, navController=navController)
        })
    })
}

@Composable
fun CardItems(item: MusicCardInfo, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate(Screen.CommentScreen.Comment.cRoute)
            }
    ) {
        Image(painter = painterResource(id = item.albumPhoto), contentDescription = item.albumName,
            modifier = Modifier
                .clip(RectangleShape)
                .size(74.dp)
                .scale(1.0f)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Column {
                Text(text = item.songName, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray))
                Text(text = item.artistName, style = TextStyle(fontSize = 21.sp,fontWeight = FontWeight.Bold, color = Color.DarkGray))
                Text(text = item.albumName, style = TextStyle(fontSize = 18.sp))
                Text(text = item.whoShared, style = TextStyle(fontSize = 18.sp))
                Text(text = item.location, style = TextStyle(fontSize = 18.sp))
            }
        }
    }
}