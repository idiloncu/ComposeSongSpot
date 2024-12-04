package com.example.composesongspot.ui.theme.drawerSc

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.composesongspot.ui.theme.ViewModel.AuthState
import com.example.composesongspot.ui.theme.ViewModel.AuthViewModel
import com.example.composesongspot.R

@Composable
fun Signup(navController: NavController,authViewModel: AuthViewModel) {
    var email by remember {
        mutableStateOf("")
    }
    var name by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var firstName by remember {
        mutableStateOf("")
    }
    var lastName by remember {
        mutableStateOf("")
    }
    var authState = authViewModel.authState.observeAsState()
    val context  = LocalContext.current
    
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                navController.navigate("Sign In")
            }
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_LONG).show()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.userprfl), contentDescription = "login image",
            modifier = Modifier.size(200.dp)
        )

        Text(text = "Hey there,", color = Color.DarkGray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Create an Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = firstName, onValueChange = {
            firstName = it
        }, label = {
            Text(text = "First Name")
        }, textStyle = TextStyle(color = Color.DarkGray))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
            },
            label = {
                Text(text = "Last Name")
            },
            textStyle = TextStyle(color = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email Address")
            },
            textStyle = TextStyle(color = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "Password")
            },
            textStyle = TextStyle(color = Color.DarkGray),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = {
            authViewModel.signUp(firstName ,email, password)
        },
            colors = ButtonDefaults.buttonColors(Color.DarkGray)) {
            Text(text = "Register", color = Color.White)
        }
    }
}