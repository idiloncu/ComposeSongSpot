package com.example.composesongspot.ui.theme.data

data class MessageData(
    val id: String = "",
    val senderId: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val senderName: String = "",
    val receiverId: String = ""
)
