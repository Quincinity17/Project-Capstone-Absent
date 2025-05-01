package com.example.absentapp.ui.screens.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.navigation.BottomNavigationBar
import com.example.absentapp.ui.screens.absent.AbsenceViewModel
import com.example.absentapp.ui.screens.absent.AbsentPage
import com.example.absentapp.ui.screens.homepage.HomePage
import com.example.absentapp.ui.screens.riwayat.RiwayatPage
import com.example.absentapp.ui.screens.setting.SettingPage
import com.example.absentapp.ui.theme.LocalAppColors
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

/**
 * Halaman utama (MainScreen) yang membungkus semua fitur navigasi bawah (Bottom Navigation)
 * dan mengarahkan ke halaman-halaman utama: Home, Riwayat, Perizinan, dan Setting.
 *
 * @param authViewModel ViewModel untuk autentikasi pengguna
 * @param absenceViewModel ViewModel untuk data absensi
 * @param locationViewModel ViewModel untuk pelacakan lokasi
 * @param rootNavController NavController dari root host (biasanya dikirim dari NavigationHost utama)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    absenceViewModel: AbsenceViewModel,
    locationViewModel: LocationViewModel,
    rootNavController: NavHostController
) {
    val bottomNavController = rememberNavController()
    val appColors = LocalAppColors.current
    val bottomBarFocusRequester = remember { FocusRequester() }

    /**
     * Aksesibilitas: Fokus otomatis ke bottom navigation bar saat pertama kali muncul
     */
    LaunchedEffect(Unit) {
        delay(150)
        bottomBarFocusRequester.requestFocus()
    }

    /**
     * Scaffold utama dengan BottomNavigationBar dan konten berdasarkan route aktif
     */
    Scaffold(
        containerColor = appColors.primaryBackground,
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                focusRequester = bottomBarFocusRequester
            )
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
            /**
             * Route: Halaman Beranda (HomePage)
             */
            composable(
                route = "homePage?fromBottomBar={fromBottomBar}",
                arguments = listOf(navArgument("fromBottomBar") { defaultValue = "false" })
            ) { backStackEntry ->
                val fromBottomBar = backStackEntry.arguments?.getString("fromBottomBar") == "true"
                HomePage(
                    fromBottomBar = fromBottomBar,
                    authViewModel = authViewModel,
                    locationViewModel = locationViewModel,
                    navController = rootNavController
                )
            }

            /**
             * Route: Halaman Riwayat Absensi
             */
            composable(
                route = "riwayatPage?fromBottomBar={fromBottomBar}",
                arguments = listOf(navArgument("fromBottomBar") { defaultValue = "false" })
            ) { backStackEntry ->
                val fromBottomBar = backStackEntry.arguments?.getString("fromBottomBar") == "true"
                RiwayatPage(
                    fromBottomBar = fromBottomBar,
                    authViewModel = authViewModel
                )
            }

            /**
             * Route: Halaman Pengajuan Perizinan
             */
            composable(
                route = "absentPage?fromBottomBar={fromBottomBar}",
                arguments = listOf(navArgument("fromBottomBar") { defaultValue = "false" })
            ) { backStackEntry ->
                val fromBottomBar = backStackEntry.arguments?.getString("fromBottomBar") == "true"
                AbsentPage(
                    fromBottomBar = fromBottomBar,
                    absenceViewModel = absenceViewModel,
                    authViewModel = authViewModel,
                    navController = rootNavController
                )
            }

            /**
             * Route: Halaman Pengaturan
             */
            composable(
                route = "settingPage?fromBottomBar={fromBottomBar}",
                arguments = listOf(navArgument("fromBottomBar") { defaultValue = "false" })
            ) { backStackEntry ->
                val fromBottomBar = backStackEntry.arguments?.getString("fromBottomBar") == "true"
                SettingPage(
                    fromBottomBar = fromBottomBar,
                    locationViewModel = locationViewModel,
                    absenceViewModel = absenceViewModel,
                    authViewModel = authViewModel,
                    rootNavController = rootNavController
                )
            }
        }
    }
}
