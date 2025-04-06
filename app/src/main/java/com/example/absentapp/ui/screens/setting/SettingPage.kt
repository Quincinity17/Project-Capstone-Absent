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

@Composable
fun SettingPage(authViewModel: AuthViewModel, navController: NavController) {
    val context = LocalContext.current
    val notificationPref = remember { NotificationPreference(context) }
    val scope = rememberCoroutineScope()
    val isNotificationEnabled by notificationPref.isNotificationEnabled.collectAsState(initial = true)

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
        Button(onClick = {
            authViewModel.signout()
            navController.navigate("login")
        }) {
            Text("Logout")
        }
    }
}


