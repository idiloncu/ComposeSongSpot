package com.example.composesongspot.ui.theme

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesongspot.R

@Composable
fun SignIn() {
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
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
        Text(
            text = "Welcome Back",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Login to your account", color = Color.DarkGray)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = email, onValueChange = {
            email = it
        }, label = {
            Text(text = "Email Address")
        }, textStyle = TextStyle(color = Color.DarkGray))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "Password")
            },
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle(color = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = {
            Log.i("SignIn", "Email: $email-- Password: $password")
        }, colors = ButtonDefaults.buttonColors(Color.DarkGray)) {
            Text(text = "Log In", color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))

        Text(text = "Forget Password", modifier = Modifier.clickable {
        }, color = Color.DarkGray, textDecoration = TextDecoration.Underline)

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(text = "Don't have an account?", color = Color.DarkGray)
            Text(text = "Sign Up", modifier = Modifier.clickable {
            }, color = Color.DarkGray, textDecoration = TextDecoration.Underline)
        }
    }
}