package com.example.composesongspot.ui.theme.ViewModel

import androidx.lifecycle.ViewModel
import com.example.composesongspot.ui.theme.data.MessageData
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    private val _messages = MutableStateFlow<List<MessageData>>(emptyList())
    val message = _messages.asStateFlow()
    val db = Firebase.database

    fun sendMessage(channelID: String, messageText: String) {
        val message = MessageData(
            db.reference.push().key ?: UUID.randomUUID().toString(),
            Firebase.auth.currentUser?.uid ?: "",
            messageText,
            System.currentTimeMillis(),
            Firebase.auth.currentUser?.displayName ?: ""
        )
        db.reference.child("channel").child("message").push().setValue(message)
    }


    fun listenForMessages(channelID: String) {
        db.getReference("channel").child(channelID).child("message").push()
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<MessageData>()
                    snapshot.children.forEach { data ->
                        val message = data.getValue(MessageData::class.java)
                        message?.let { list.add(it) }
                        db.getReference("channel").child(channelID).child("message").push()

                    }
                    _messages.value = list
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}