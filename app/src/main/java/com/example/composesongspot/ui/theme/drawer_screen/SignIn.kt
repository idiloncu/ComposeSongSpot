package com.example.composesongspot.ui.theme.drawer_screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.composesongspot.R
import com.example.composesongspot.ui.theme.ViewModel.AuthState
import com.example.composesongspot.ui.theme.ViewModel.AuthViewModel

@Composable
fun SignIn(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    val authState = authViewModel.authState.observeAsState()
    val context  = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("Home")
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
        Text(
            text = stringResource(R.string.welcome_back),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.login_to_your_account), color = Color.DarkGray)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = email, onValueChange = {
            email = it
        }, label = {
            Text(text = stringResource(R.string.email_address))
        }, textStyle = TextStyle(color = Color.DarkGray))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = stringResource(R.string.password))
            },
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle(color = Color.DarkGray),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = {
            authViewModel.login(email, password)
        },
            colors = ButtonDefaults.buttonColors(Color.DarkGray)) {
            Text(text = stringResource(R.string.log_in), color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))

        Text(text = stringResource(R.string.forget_password), modifier = Modifier.clickable {
            navController.navigate("Sign Up")
        }, color = Color.DarkGray, textDecoration = TextDecoration.Underline)

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(text = stringResource(R.string.dont_have_an_account), color = Color.DarkGray)
            Text(text = stringResource(R.string.sign_up), modifier = Modifier.clickable {
                navController.navigate("Sign Up")
            }, color = Color.DarkGray, textDecoration = TextDecoration.Underline)
        }
    }
}