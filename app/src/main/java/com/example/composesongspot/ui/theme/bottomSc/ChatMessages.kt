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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.composesongspot.ui.theme.ViewModel.ChatViewModel
import com.example.composesongspot.ui.theme.data.MessageData
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun ChatScr(
    navController: NavController,
    channelId: String,
    viewModel: ChatViewModel = viewModel()
) {

    LaunchedEffect(key1 = true) {
        viewModel.listenForMessages(channelId)
    }
    val messages = viewModel.message.collectAsState()
    ChatMessages(messages = messages.value,
        onSendMessage = { message ->
            viewModel.sendMessage(channelId, message)
        })
    }

@Composable
fun ChatMessages(
    messages: List<MessageData>,
    onSendMessage: (String) -> Unit
) {
    val hideKeyboardController = LocalSoftwareKeyboardController.current
    val messages = remember { mutableStateListOf<MessageData>() }
    val messageText = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            items(messages) { message ->
                MessageItem(message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            TextField(
                value = messageText.value,
                onValueChange = { messageText.value = it },
                placeholder = { Text("Type a message...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        hideKeyboardController?.hide()
                    }
                )

            )


            IconButton(
                onClick = {
                    if (messageText.value.isNotEmpty()) {
                        messages.add(MessageData(message = messageText.value, senderId = "You"))
                        messageText.value = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}

//Chat bubble
@Composable
fun MessageItem(message: MessageData) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
    val backgroundColor = if (isCurrentUser) Color.Green else Color.White
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


