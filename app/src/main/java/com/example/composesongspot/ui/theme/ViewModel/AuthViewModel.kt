package com.example.composesongspot.ui.theme.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.composesongspot.ui.theme.data.ChatInfo
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    private val firebaseDatabase = Firebase.database
    private val auth = FirebaseAuth.getInstance()
    private var mDbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference()
    private val _userList = MutableStateFlow<List<ChatInfo>>(emptyList())
    val userList = _userList.asStateFlow()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
        fetchUsers()
    }

    //getChannels
    fun fetchUsers() {
        firebaseDatabase.getReference("user").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<ChatInfo>()
            snapshot.children.forEach { data ->
                val map = data.value as? Map<*, *> ?: return@forEach
                val chatInfo = ChatInfo(
                    id = map["uid"] as? String ?: "",
                    name = map["name"] as? String ?: "Unknown", // Boş veya eksik `name` durumunda varsayılan değer
                    email = map["email"] as? String ?: "",
                    createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
                )
                list.add(chatInfo)
            }
            _userList.value = list
        }
    }

    fun addUserToDatabase(name: String, email: String, uid: String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(uid).setValue(ChatInfo(name, email, uid))
    }


    fun checkAuthStatus() {
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