package com.example.composesongspot.ui.theme.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.composesongspot.ui.theme.data.Group
import com.example.composesongspot.ui.theme.data.GroupMessageData
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
    private val _groupChats = MutableStateFlow<List<GroupMessageData>>(emptyList())
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

    fun sendGroupMessage(groupID: String, messageText: String, senderId: String) {
        val id = UUID.randomUUID()
        val gmessage = GroupMessageData(
            messageId = UUID.randomUUID().toString(),
            groupId = id.toString(),
            senderId = senderId,
            System.currentTimeMillis(),
            messageText
        )
        db.reference.child("GroupMessaging").child("groupMessage").child(groupID).push()
            .setValue(gmessage)
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

    fun createGroup(
        members: List<UserData>,
        groupName: String
    ) {

        val groupId = UUID.randomUUID().toString()
        val groupChat = Group(
            groupId = groupId,
            groupName = groupName,
            participants = members
        )
        dbRef.child("GroupMessaging").child("group").child(groupId).setValue(groupChat)
    }

    fun listenGroupChats(groupID: String) {
        dbRef.child("GroupMessaging").child("groupMessage").child(groupID).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupMessageList = mutableListOf<GroupMessageData>()
                    snapshot.children.forEach { data ->
                        val map = data.value as? Map<*, *> ?: return@forEach
                        val groupData = GroupMessageData(
                            groupId = map["groupId"] as? String ?: "",
                            messageId = map["messageId"] as? String ?: "",
                            senderId = map["senderId"] as? String ?: "",
                            message = map["message"] as? String ?: "",
                        )
                        groupMessageList.add(groupData)
                    }
                    _groupChats.value = groupMessageList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                }
            })
    }
}