package com.example.composesongspot.ui.theme.drawer_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.composesongspot.R
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
        Text(text = stringResource(R.string.want_to_sign_out), fontSize = 15.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Button(onClick = {
            authViewModel.signOut()
        }) {
            Text(text = stringResource(R.string.sign_out))

        }
    }
}