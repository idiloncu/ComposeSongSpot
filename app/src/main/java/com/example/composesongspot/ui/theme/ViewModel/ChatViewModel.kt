package com.example.composesongspot.ui.theme.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.composesongspot.ui.theme.data.GroupChatData
import com.example.composesongspot.ui.theme.data.MessageData
import com.example.composesongspot.ui.theme.data.UserData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
    private val _groupChats = MutableStateFlow<List<GroupChatData>>(emptyList())
    val groupChats = _groupChats.asStateFlow()
    private val db = Firebase.database
    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference()

    fun sendMessage(channelID: String, messageText: String) {
        val message = MessageData(
            db.reference.push().key ?: UUID.randomUUID().toString(),
            Firebase.auth.currentUser?.uid ?: "",
            messageText,
            System.currentTimeMillis(),
            Firebase.auth.currentUser?.displayName ?: "",
            receiverId = channelID
        )
        db.reference.child("messages").child(channelID.takeLast(5)).push().setValue(message)
    }

    fun listenForMessages(channelID: String) {
        dbRef.child("messages").child(channelID.takeLast(5)).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<MessageData>()
                    snapshot.children.forEach { data ->
                        val message = data.getValue(MessageData::class.java)
                        message?.let {
                            if (it.senderId == channelID && it.receiverId == FirebaseAuth.getInstance().currentUser?.uid) {
                                list.add(it)
                            } else {
                                if (it.receiverId == channelID && it.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                                    list.add(it)
                                }
                            }
                        }
                    }
                    _messages.value = list
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                }
            })
    }

    fun createGroupChat(
        groupID: String,
        members: List<UserData>,
        groupMessage: String,
        groupName: String
    ) {
        val groupId = dbRef.child("groupChats").push().key ?: UUID.randomUUID().toString()
        val membersId = members.joinToString(",") { it.id } // Üyelerin UID'lerini birleştir
        val groupChat = GroupChatData(
            //grupId= sender 1 tane
            groupId = groupID,
            groupName = groupName,
            membersId = membersId, //->receiver(alıcı) many
            messages = groupMessage
        )
        dbRef.child("profile").child("groupChats").child(groupId).setValue(groupChat)

    }

    fun listenGroupChats(groupID: String) {
        dbRef.child("profile").child("groupChats").child(groupID).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val glist = mutableListOf<GroupChatData>()
                    snapshot.children.forEach { data ->
                        val message = data.getValue(GroupChatData::class.java)
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                        message?.let {
                            if (it.membersId.split(",").contains(currentUserId)) {
                                glist.add(it)
                            }
                        }
                    }
                    _groupChats.value = glist
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                }
            })
    }
}