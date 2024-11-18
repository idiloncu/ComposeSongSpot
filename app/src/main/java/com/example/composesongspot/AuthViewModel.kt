package com.example.composesongspot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.composesongspot.ui.theme.bottomSc.ChatInfo
import com.example.composesongspot.ui.theme.data.UserChat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
//        userList = ArrayList()
//        adapterItem= Screen.BottomScreen.Message
    }

    //getChannels
    fun fetchUsers() {
        firebaseDatabase.getReference("user").get().addOnSuccessListener {
            val list = mutableListOf<ChatInfo>()
            it.children.forEach { data ->
                val chatInfo = ChatInfo(data.key!!, data.value.toString())
                list.add(chatInfo)
            }
            _userList.value = list
        }
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
                    //    addUserToDatabase(name, email, auth.currentUser?.uid!!)

                    } else {
                        _authState.value =
                            AuthState.Error(task.exception?.message ?: "Something went wrong")
                    }
                }
        }

//         fun addUserToDatabase(name: String, email: String, uid: String) {
//            mDbRef = FirebaseDatabase.getInstance().getReference()
//            mDbRef.child("user").child(uid).setValue(UserChat(name, email, uid))
//        }

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
