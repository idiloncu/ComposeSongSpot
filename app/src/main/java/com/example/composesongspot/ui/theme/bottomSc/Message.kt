package com.example.composesongspot.ui.theme.bottomSc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.composesongspot.ui.theme.ViewModel.AuthViewModel
import com.example.composesongspot.Screen
import com.example.composesongspot.ui.theme.data.ChatInfo

@Composable
fun Message(navController: NavController) {
    LazyColumnChat(navController, viewModel())
}

@Composable
fun LazyColumnChat(navController: NavController,viewModel: AuthViewModel = viewModel()) {
    val userList = viewModel.userList.collectAsState()

    LazyColumn(modifier = Modifier.padding(8.dp)){
        items(userList.value){user->
            ChatCardItems( user,navController)

        }
    }
}

@Composable
fun ChatCardItems(item: ChatInfo, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                 navController.navigate(Screen.ChatScreen.ChatPage.route)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Column {
                Text(text = item.name, style = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color.Black))
            }
        }
    }
}