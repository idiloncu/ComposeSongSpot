package com.example.composesongspot.ui.theme.ViewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.composesongspot.Screen

class MainViewModel:ViewModel(){
    private val _currentScreen : MutableState<Screen> = mutableStateOf(Screen.DrawerScreen.SignIn)
    val currentScreen : MutableState<Screen>
        get() =_currentScreen
    fun setCurrentScreen(screen: Screen){
        _currentScreen.value = screen
    }
}