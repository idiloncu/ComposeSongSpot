package com.example.composesongspot.ui.theme.drawerSc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.composesongspot.ui.theme.ViewModel.AuthState
import com.example.composesongspot.ui.theme.ViewModel.AuthViewModel

@Composable
fun SignOut(navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("Home")

            else -> Unit
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "If you want to sign out, please click the button below", fontSize = 15.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Button(onClick = {
            authViewModel.signOut()
        }) {
            Text(text = "Sign Out")

        }
    }
}