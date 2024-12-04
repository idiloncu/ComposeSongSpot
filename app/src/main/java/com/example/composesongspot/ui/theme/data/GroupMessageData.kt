package com.example.composesongspot.ui.theme.data

data class GroupMessageData(
    val messageId: String,
    val groupId: String,
    val senderId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val message: String = ""
)
