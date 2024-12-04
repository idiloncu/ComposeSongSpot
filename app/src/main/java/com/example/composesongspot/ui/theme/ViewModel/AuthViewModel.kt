package com.example.composesongspot.ui.theme.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.composesongspot.ui.theme.data.Group
import com.example.composesongspot.ui.theme.data.UserData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    private val firebaseDatabase = Firebase.database
    private val auth = FirebaseAuth.getInstance()
    private var mDbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference()
    private val _userList = MutableStateFlow<List<UserData>>(emptyList())
    val userList = _userList.asStateFlow()
    private val _groupList = MutableStateFlow<List<Group>>(emptyList())
    val groupList = _groupList.asStateFlow()
    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference()
    private val groupId = dbRef.child("groupChats").push().key ?: UUID.randomUUID().toString()
    private val members = mutableListOf<UserData>()
    private val membersId = members.joinToString(",") { it.id } // Üyelerin UID'lerini birleştir

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
        fetchUsers()
        fetchGroups()
    }

    private fun fetchUsers() {
        firebaseDatabase.getReference("profile").child("user").get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<UserData>()
                snapshot.children.forEach { data ->
                    val map = data.value as? Map<*, *> ?: return@forEach
                    val userData = UserData(
                        id = map["id"] as? String ?: "",
                        name = map["name"] as? String ?: "Unknown",
                        email = map["email"] as? String ?: "",
                        createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
                    )
                    list.add(userData)
                }
                _userList.value = list
            }
    }

    private fun fetchGroups() {
        firebaseDatabase.getReference("GroupMessaging").child("group").get()
            .addOnSuccessListener { snapshot ->
                val glist = mutableListOf<Group>()
                snapshot.children.forEach { group ->
                    val map = group.value as? Map<*, *> ?: return@forEach
                    val groupData = Group(
                        groupId = map["groupId"] as? String ?: "",
                        groupName = map["groupName"] as? String ?: "Unknown",
                        participants = map["participants"] as? List<UserData> ?: emptyList()
                    )
                    glist.add(groupData)
                }
                _groupList.value = glist
            }
            .addOnFailureListener { error ->
                Log.e("fetchGroups", "Error fetching groups: ${error.message}")
            }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("profile").child("user").child(uid)
            .setValue(UserData(email = email, name = name, id = uid))
    }


    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signUp(name: String, email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    )
                    _authState.value = AuthState.Authenticated
                    addUserToDatabase(name, email, auth.currentUser?.uid!!)

                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
