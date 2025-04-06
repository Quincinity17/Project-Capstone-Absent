package com.example.absentapp.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.navigation.BottomNavigationBar
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.ui.screens.ProfilePage
import com.example.absentapp.ui.screens.absent.AbsenPage
import com.example.absentapp.ui.screens.setting.SettingPage

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Homepage(modifier: Modifier = Modifier,
             navController: NavController,
             authViewModel: AuthViewModel,
             locationViewModel: LocationViewModel
) {
    val innerNavController = rememberNavController()

    LaunchedEffect(Unit) {
        authViewModel.getAbsentTime()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp), // Ini matiin auto padding dari status bar
                bottomBar = {
            BottomNavigationBar(innerNavController)
        }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = "absen",
            modifier = Modifier.padding(padding)

        ) {
            composable("absen") {
                // pass data dan viewmodel ke AbsenPage
                AbsenPage(authViewModel, locationViewModel, navController)
            }
            composable("profile") {
                ProfilePage(authViewModel, navController)
            }
            composable("setting") {
                SettingPage(authViewModel, navController)
            }
        }
    }
}
