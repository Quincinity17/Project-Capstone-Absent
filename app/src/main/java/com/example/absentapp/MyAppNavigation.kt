package com.example.absentapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.absentapp.pages.Homepage
import com.example.absentapp.pages.LoginPage
import com.example.absentapp.pages.SignupPage

//import np.com.bimalkafle.firebaseauthdemoapp.pages.HomePage
//import np.com.bimalkafle.firebaseauthdemoapp.pages.LoginPage
//import np.com.bimalkafle.firebaseauthdemoapp.pages.SignupPage


@Composable
fun MyAppNavigation (modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home") {
            Homepage(modifier, navController, authViewModel)
        }
    })
}