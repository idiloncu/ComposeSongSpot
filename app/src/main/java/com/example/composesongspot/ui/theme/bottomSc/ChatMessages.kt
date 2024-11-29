package com.example.composesongspot.ui.theme.bottomSc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.composesongspot.R
import com.example.composesongspot.ui.theme.ViewModel.AuthViewModel
import com.example.composesongspot.ui.theme.ViewModel.ChatViewModel
import com.example.composesongspot.ui.theme.data.GroupChatData
import com.example.composesongspot.ui.theme.data.MessageData
import com.example.composesongspot.ui.theme.data.UserData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun ChatScr(
    navController: NavController,
    receiverId: String,
) {

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val viewModel: ChatViewModel = viewModel()
            LaunchedEffect(key1 = true) {
                viewModel.listenForMessages(receiverId)
            }
            val messages = viewModel.message.collectAsState()
            ChatMessages(
                messages = messages.value,
                onSendMessage = { message ->
                    viewModel.sendMessage(receiverId, message)
                }
            )
        }
    }
}

@Composable
fun GroupChatScr(
    navController: NavController,
    groupID: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Group Chat")
                },
                backgroundColor = Color.Gray,
                contentColor = Color.White
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val groupChatViewModel: ChatViewModel = viewModel()
            LaunchedEffect(key1 = true) {
                groupChatViewModel.listenGroupChats(groupID)
            }
//TODO : mesajlar burada degil GroupMessage sayfasında gorunecek
            val groupChats = groupChatViewModel.groupChats.collectAsState()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val members = listOfNotNull(currentUser?.uid) // Sadece mevcut kullanıcı
            val inputGroupName = remember { mutableStateOf("") }

            GroupMessage(
                groupMessage = groupChats.value,
                onSendMessage = { message ->
                    groupChatViewModel.createGroupChat(
                        groupName = inputGroupName.value,
                        members = members.map { member ->
                            UserData(id = member, name = currentUser?.displayName ?: "")
                        },
                        groupID = groupID,
                        groupMessage = message
                    )
                }
            )
        }
    }
}

@Composable
fun ChatMessages(
    messages: List<MessageData>,
    onSendMessage: (String) -> Unit = {}
) {
    val hideKeyboardController = LocalSoftwareKeyboardController.current
    val msg = remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(messages) { message ->
                MessageItem(message = message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .background(Color.LightGray), verticalAlignment = Alignment.CenterVertically
        ) {

            TextField(
                value = msg.value,
                onValueChange = { msg.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Type a message") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    hideKeyboardController?.hide()
                })
            )
            IconButton(onClick = {
                onSendMessage(msg.value)
                msg.value = ""
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "send")
            }
        }
    }
}

@Composable
fun GroupMessage(
    groupMessage: List<GroupChatData>,
    onSendMessage: (String) -> Unit = {}
) {
    val hideKeyboardController = LocalSoftwareKeyboardController.current
    val gMsg = remember { mutableStateOf("") }
    val openDialog = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(groupMessage) { gMessage ->
                GroupMessageItem(gMessage = gMessage)
            }
        }
        Row {

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .background(Color.LightGray), verticalAlignment = Alignment.CenterVertically
        ) {

            TextField(
                value = gMsg.value,
                onValueChange = { gMsg.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Type a message your group") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    hideKeyboardController?.hide()
                })
            )
            Row {
                IconButton(onClick = {
                    onSendMessage(gMsg.value)
                    gMsg.value = ""
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "send")
                }

                IconButton(onClick = {
                    openDialog.value = true

                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.person_add),
                        contentDescription = "add_people"
                    )
                }
            }
        }
    }
    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            confirmButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("User List") },
            text = {
                UserList()
                UserListWithCheckBox()
            }
        )
    }
}

@Composable
fun UserList(viewModel: AuthViewModel = viewModel()) {
    val userList = viewModel.userList.collectAsState()
    LazyColumn {
        items(userList.value) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = user.name, style = MaterialTheme.typography.body1)
            }
        }
    }
}

@Composable
fun UserListWithCheckBox(viewModel: AuthViewModel = viewModel()) {
    val userList = viewModel.userList.collectAsState()
    val selectedUsers = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn {
        items(userList.value) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Checkbox(
                    checked = selectedUsers[user.id] == true,
                    onCheckedChange = { isChecked ->
                        selectedUsers[user.id] = isChecked
                    }
                )
            }
        }
    }
}

//Chat bubble
@Composable
fun MessageItem(message: MessageData) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
    val backgroundColor = if (isCurrentUser) Color.Green else Color.Black
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .background(
                    color = Color.Green.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color.Green,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.message,
                style = MaterialTheme.typography.body1,
                color = Color.Black,
            )
        }

        Text(
            text = message.senderId,
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}

//Group chat bubble
@Composable
fun GroupMessageItem(gMessage: GroupChatData) {
    val isCurrentUser = gMessage.groupId == Firebase.auth.currentUser?.uid
    val backgroundColor = if (isCurrentUser) Color.Green else Color.Black
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .background(
                    color = Color.Green.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color.Green,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = gMessage.messages,
                style = MaterialTheme.typography.body1,
                color = Color.Black,
            )
        }

        Text(
            text = gMessage.groupId,
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}


