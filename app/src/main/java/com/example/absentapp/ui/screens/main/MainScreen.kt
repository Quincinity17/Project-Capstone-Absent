package com.example.absentapp.ui.screens.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.navigation.BottomNavigationBar
import com.example.absentapp.ui.screens.homepage.HomePage
import com.example.absentapp.ui.screens.riwayat.RiwayatPage
import com.example.absentapp.ui.screens.setting.SettingPage
import com.example.absentapp.ui.theme.LocalAppColors

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    rootNavController: NavController
) {
    val bottomNavController = rememberNavController()
    val appColors = LocalAppColors.current


    Scaffold(
        containerColor = appColors.primaryBackground,
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "homePage",
            modifier = Modifier.padding(padding)
        ) {
            composable("homePage") {
                HomePage(
                    authViewModel = authViewModel,
                    locationViewModel = locationViewModel,
                    navController = rootNavController
                )
            }
            composable("riwayatPage") {
                RiwayatPage(authViewModel = authViewModel)
            }
            composable("settingPage") {
                SettingPage(
                    authViewModel = authViewModel,
                    rootNavController = rootNavController
                )
            }

        }
    }
}
