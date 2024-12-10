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
    private val firebaseDatabase = Firebase.database
    private val _userList = MutableStateFlow<List<UserData>>(emptyList())
    val userList = _userList.asStateFlow()

    fun sendMessage(channelID: String, messageText: String) {
        val message = MessageData(
            db.reference.push().key ?: UUID.randomUUID().toString(),
            Firebase.auth.currentUser?.uid ?: "",
            messageText,
            System.currentTimeMillis(),
            Firebase.auth.currentUser?.displayName ?: "",
            receiverId = channelID
        )
        db.reference.child("messages").child(channelID).push().setValue(message)
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
        dbRef.child("messages").child(channelID).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<MessageData>()
                    snapshot.children.forEach { data ->
                        val message = data.getValue(MessageData::class.java)
                        message?.let {
                            if (it.senderId == channelID || it.receiverId == FirebaseAuth.getInstance().currentUser?.uid) {
                                list.add(it)
                            } else {
                                if (it.receiverId == channelID || it.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                                    list.add(it)
                                }
                                Log.d("mesajj", "senderId: ${it.senderId}")
                                Log.d("mesajj", "senderName: ${it.senderName}")
                                Log.d("mesajj", "receiverId: ${it.receiverId}")

                            }
                        }
                    }
                    _messages.value = list
                    Log.d("mesajj", "channelID: $channelID")
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

    fun addUserToGroup(groupId: String, user: UserData) {
        val groupRef = firebaseDatabase.getReference("GroupMessaging").child("group").child(groupId)
        groupRef.child("participants").get().addOnSuccessListener { snapshot ->
            val currentParticipants =
                snapshot.children.mapNotNull { it.getValue(UserData::class.java) }.toMutableList()
            if (currentParticipants.none { it.id == user.id }) {
                currentParticipants.add(user)
                groupRef.child("participants").setValue(currentParticipants)
            }
        }.addOnFailureListener { error ->
            Log.e("addUserToGroup", "Error adding user: ${error.message}")
        }
    }
}