package com.example.absentapp.ui.screens.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.navigation.BottomNavigationBar
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.ui.screens.ProfilePage
import com.example.absentapp.ui.screens.absent.AbsenPage
import com.example.absentapp.ui.screens.setting.SettingPage
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absentapp.R


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Homepage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel
) {
    val innerNavController = rememberNavController()
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.getAbsentTime()
    }


    val currentRoute = innerNavController.currentBackStackEntryAsState().value?.destination?.route

    // ⬇️ Handle tombol back hanya jika di tab absen
    BackHandler(enabled = currentRoute == "absen") {
        showExitDialog = true
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showExitDialog) {
        ModalBottomSheet(
            onDismissRequest = { showExitDialog = false },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ilustrasi custom (opsional)
                Image(
                    painter = painterResource(R.drawable.ilt_exit), // ilustrasi keluar
                    contentDescription = "Exit App",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Keluar Aplikasi", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Apakah Anda yakin ingin keluar dari aplikasi?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            showExitDialog = true
                            android.os.Process.killProcess(android.os.Process.myPid())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                        ,
                        shape = RoundedCornerShape(8.dp),

                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF022D9B))
                    ) {
                        Text("Ya, saya yakin", color = Color.White)
                    }

                    Button(
                        onClick = {
                            showExitDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 2.dp,
                                color = Color(0xFF0A6EFF), // ganti dengan warna biru custom kamu
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0A6EFF) // teksnya biru juga
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp) // biar flat
                    ) {
                        Text("Tidak")
                    }

                }

            }
        }
    }


    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
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
