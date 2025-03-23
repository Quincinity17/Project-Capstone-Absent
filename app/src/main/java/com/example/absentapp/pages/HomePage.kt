package com.example.absentapp.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.absentapp.AuthState
import com.example.absentapp.AuthViewModel
//import np.com.bimalkafle.firebaseauthdemoapp.AuthState
//import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel

@Composable
fun Homepage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel){

    val authState = authViewModel.authState.observeAsState()
    val absenTime = authViewModel.absenTime.observeAsState()


    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }
//
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (absenTime.value != null) {
            Text(
                text = "Hari ini kamu sudah absen pada jam ${absenTime.value}",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            authViewModel.absen()
        }) {
            Text(text = "Absen Sekarang")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            authViewModel.signout()
        }) {
            Text(text = "Sign out")
        }
    }

    val context = LocalContext.current
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Success) {
            Toast.makeText(context, (authState.value as AuthState.Success).message, Toast.LENGTH_SHORT).show()
        }
        if (authState.value is AuthState.Error) {
            Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

}