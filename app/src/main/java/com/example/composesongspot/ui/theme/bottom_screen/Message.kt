package com.example.composesongspot.ui.theme.bottom_screen

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
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.AlertDialog
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
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
import com.example.composesongspot.ui.theme.ViewModel.ChatViewModel
import com.example.composesongspot.ui.theme.data.Group
import com.example.composesongspot.ui.theme.data.UserData
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

@Composable
fun Message(navController: NavController) {
    LazyColumnChat(navController, viewModel())
    FabButton(groupChatViewModel = ChatViewModel())
}

@Composable
fun LazyColumnChat(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val userList = viewModel.userList.collectAsState()
    val groupChatViewModel: ChatViewModel = viewModel()
    val groupList = viewModel.groupList.collectAsState()

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(groupList.value) { group ->
            ChatCardItems(groupItem = group, item = null, navController = navController)
        }
        items(userList.value) { user ->
            ChatCardItems(groupItem = null, user, navController)
        }
    }
    FabButton(groupChatViewModel)
}

@Composable
fun ChatCardItems(groupItem: Group?, item: UserData?, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                if (item != null) {
                    navController.navigate("chat/${item.id}")
                }
                if (groupItem != null) {
                    navController.navigate("group/${groupItem.groupId}")
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
                if (item != null) {
                    Text(
                        text = item.name,
                        style = TextStyle(
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    )
                } else {
                    Text(
                        text = groupItem!!.groupName,
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
}

@Composable
fun FabButton(groupChatViewModel: ChatViewModel) {
    val openDialog = remember { mutableStateOf(false) }
    val inputGroupName = remember { mutableStateOf("") }
    val groupList = remember { mutableStateListOf<String>() }
    val context = LocalContext.current.applicationContext

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            modifier = Modifier
                .padding(end = 25.dp, bottom = 40.dp)
                .align(Alignment.BottomEnd),
            containerColor = colorResource(id = R.color.purple_500),
            onClick = {
                openDialog.value = true
            }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        }
        if (openDialog.value) {
            AlertDialog(onDismissRequest = { openDialog.value = false },
                confirmButton = {
                    TextButton(onClick = {
                        if (inputGroupName.value.isNotBlank()) {
                            val groupID = UUID.randomUUID().toString() // grup id
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val members = listOfNotNull(currentUser?.uid) // Sadece mevcut kullanıcı

                            groupChatViewModel.createGroup(
                                members = listOf(
                                    UserData(
                                        currentUser?.email.toString(),
                                        currentUser?.displayName.toString(),
                                        currentUser!!.uid,
                                        System.currentTimeMillis()
                                    )
                                ),
                                groupName = inputGroupName.value
                            )

                            groupList.add(inputGroupName.value)
                            inputGroupName.value = ""
                            openDialog.value = false
                        } else {
                            Toast.makeText(context,
                                context.getString(R.string.dont_leave_it_blank), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openDialog.value = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                title = { Text(stringResource(R.string.create_a_group)) },
                text = {
                    Column {
                        TextField(
                            value = inputGroupName.value,
                            onValueChange = { inputGroupName.value = it },
                            placeholder = { Text(stringResource(R.string.group_name)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                })
        }
    }
}
