package com.example.composesongspot.ui.theme.bottomSc

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.composesongspot.R
import com.example.composesongspot.ui.theme.ViewModel.AuthViewModel
import com.example.composesongspot.ui.theme.data.GroupChatData
import com.example.composesongspot.ui.theme.data.UserData

@Composable
fun Message(navController: NavController) {
    LazyColumnChat(navController, viewModel())
}

@Composable
fun LazyColumnChat(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val userList = viewModel.userList.collectAsState()

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(userList.value) { user ->
            ChatCardItems(user, navController)

        }
    }
    FabButton()
}

@Composable
fun ChatCardItems(item: UserData, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate("chat/${item.id}") {

                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Column {
                Text(
                    text = item.name,
                    style = TextStyle(
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                )
            }
        }
    }
}

@Composable
fun FabButton() {
    val openDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current.applicationContext
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            modifier = Modifier
                .padding(end = 25.dp, bottom = 40.dp)
                .align(Alignment.BottomEnd),
            containerColor = colorResource(id = R.color.purple_500),
            onClick = {
                Toast.makeText(context, "FAB Clicked", Toast.LENGTH_SHORT).show()
            }){
            Icon(imageVector = Icons.Filled.Add, contentDescription =null )
        }
//        if (openDialog.value){
//            AlertDialog(onDismissRequest = { openDialog.value=false },
//                title = { Text(stringResource(R.string.create_a_group)) },
//                text = {
//                    Column {
//                        Text(stringResource(R.string.enter_group_name))
//                        TextField(value = , onValueChange =)
//                    }
//                }
//        }

    }
}
