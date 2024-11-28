package com.example.composesongspot.ui.theme.data

data class GroupChatData(
    var groupName:String,
    var groupId:String, //sender - gonderen
    val createdAt: Long = System.currentTimeMillis(),
    var membersId: String="", //receivers - alıcı many
    var messages: String = ""
)