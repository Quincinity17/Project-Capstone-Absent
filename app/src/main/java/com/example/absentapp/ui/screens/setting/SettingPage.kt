package com.example.absentapp.ui.screens.setting

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.data.dataStore.NotificationPreference
import com.example.absentapp.utils.startLocationService
import com.example.absentapp.utils.stopLocationService
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.example.absentapp.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(authViewModel: AuthViewModel, navController: NavController) {
    val context = LocalContext.current
    val notificationPref = remember { NotificationPreference(context) }
    val scope = rememberCoroutineScope()
    val isNotificationEnabled by notificationPref.isNotificationEnabled.collectAsState(initial = true)

    val showLogoutSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Reminder Switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Reminder Absensi", fontWeight = FontWeight.Bold)
                Text(
                    text = "Akan memberi notifikasi 10 menit sebelum batas absen jika belum login.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.weight(0.1f))
            Switch(
                checked = isNotificationEnabled,
                onCheckedChange = { isChecked ->
                    scope.launch {
                        notificationPref.setNotificationEnabled(isChecked)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { showLogoutSheet.value = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authViewModel.deleteAllAbsenceHistory()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hapus Semua Riwayat Absensi", color = Color.White)
        }

    }

    if (showLogoutSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showLogoutSheet.value = false },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ilt_logout), // ilustrasi keluar
                    contentDescription = "Exit App",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("LogOut Akun", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Apakah Anda yakin ingin logout akun dari aplikasi?",
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
                            showLogoutSheet.value = false
                            authViewModel.signout()
                            navController.navigate("login") {
                                popUpTo(0) // agar tidak bisa back ke halaman sebelumnya
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                2.dp,
                                Color(0xFF0A6EFF),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0A6EFF)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Ya, saya yakin")
                    }

// Tombol "Tidak" - Sekarang jadi tombol biru solid
                    Button(
                        onClick = { showLogoutSheet.value = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF022D9B))
                    ) {
                        Text("Tidak", color = Color.White)
                    }

                }
            }
        }
    }

}


