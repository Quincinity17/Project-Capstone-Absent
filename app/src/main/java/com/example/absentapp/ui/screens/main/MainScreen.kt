package com.example.absentapp.ui.screens.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.navigation.BottomNavigationBar
import com.example.absentapp.ui.screens.homepage.HomePage
import com.example.absentapp.ui.screens.riwayat.RiwayatPage
import com.example.absentapp.ui.screens.setting.SettingPage
import com.example.absentapp.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    rootNavController: NavController
) {
    val bottomNavController = rememberNavController()
    val appColors = LocalAppColors.current
    val bottomBarFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(150)
        bottomBarFocusRequester.requestFocus()
    }


    Scaffold(
        containerColor = appColors.primaryBackground,
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                focusRequester = bottomBarFocusRequester            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "homePage",
            modifier = Modifier
                .padding(padding)
                .semantics {
                    isTraversalGroup = true
                    traversalIndex = 1f
                }
        ) {
            //Halaman Homepage
            composable(
                route = "homePage?fromBottomBar={fromBottomBar}",
                arguments = listOf(
                    navArgument("fromBottomBar") {
                        defaultValue = "false"
                    }
                )
            ) { backStackEntry ->
                val fromBottomBar = backStackEntry.arguments?.getString("fromBottomBar") == "true"

                HomePage(
                    fromBottomBar = fromBottomBar,
                    authViewModel = authViewModel,
                    locationViewModel = locationViewModel,
                    navController = rootNavController
                )
            }

            //Halaman Riwayat
            composable(
                route = "riwayatPage?fromBottomBar={fromBottomBar}",
                arguments = listOf(
                    navArgument("fromBottomBar") {
                        defaultValue = "false"
                    }
                )
            ) { backStackEntry ->
                val fromBottomBar = backStackEntry.arguments?.getString("fromBottomBar") == "true"

                RiwayatPage(
                    fromBottomBar = fromBottomBar,
                    authViewModel = authViewModel
                )
            }

            //Halaman Setting
            composable(
                route = "settingPage?fromBottomBar={fromBottomBar}",
                arguments = listOf(
                    navArgument("fromBottomBar") {
                        defaultValue = "false"
                    }
                )
            ) { backStackEntry ->
                val fromBottomBar = backStackEntry.arguments?.getString("fromBottomBar") == "true"

                SettingPage(
                    fromBottomBar = fromBottomBar,
                    authViewModel = authViewModel,
                    rootNavController = rootNavController
                )
            }
        }
    }
}
