package com.example.composesongspot.ui.theme.data

import kotlinx.coroutines.flow.MutableStateFlow

data class GroupChatData(
    var groupName:String,
    var groupId:String,
    var members: List<UserData>,
    var messages: MutableStateFlow<List<MessageData>>
)