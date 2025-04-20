package com.example.absentapp.ui.screens.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.data.dataStore.NotificationPreference
import com.example.absentapp.ui.components.ConfirmationBottomSheet
import com.example.absentapp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(
    authViewModel: AuthViewModel,
    rootNavController: NavController) {
    val context = LocalContext.current
    val notificationPref = remember { NotificationPreference(context) }
    val scope = rememberCoroutineScope()
    val isNotificationEnabled by notificationPref.isNotificationEnabled.collectAsState(initial = true)
    val appColors = LocalAppColors.current


    val showLogoutSheet = remember { mutableStateOf(false) }
    val showDeleteAbsentSheet = remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Text(
            "Setting Page",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = appColors.primaryText,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Reminder Absensi",
                    fontWeight = FontWeight.Bold,
                    color = appColors.primaryText,
                )
                Text(
                    text = "Akan memberi notifikasi 10 menit sebelum batas absen jika belum login.",
                    fontSize = 12.sp,
                    color = appColors.secondaryText,
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
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = appColors.checkedThumbColor,
                    checkedTrackColor = appColors.checkedTrackColor,

                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                )
            )
        }

        Divider(color = appColors.secondaryBackground, thickness = 4.dp)

        // Hapus Riwayat
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp)
                .clickable {
                    showDeleteAbsentSheet.value = true

                }
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hapus semua riwayat absensi",
                    fontWeight = FontWeight.Bold,
                    color = appColors.primaryText,
                )
                Text(
                    text = "Anda akan menghapus semua riwayat absensi",
                    fontSize = 12.sp,
                    color = appColors.secondaryText,
                    lineHeight = 16.sp
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow),
                modifier = Modifier.size(24.dp),
                contentDescription = "Arahkan",
                tint = appColors.primaryText
            )
        }

        Divider(color = appColors.secondaryBackground, thickness = 4.dp)

        // Log Out
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp)
                .clickable {
                    showLogoutSheet.value = true
                }
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    color = appColors.primaryText,
                )
                Text(
                    text = "Anda akan keluar dari akun ini",
                    fontSize = 12.sp,
                    color = appColors.secondaryText,
                    lineHeight = 16.sp
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow),
                modifier = Modifier.size(24.dp),
                contentDescription = "Arahkan",
                tint = appColors.primaryText
            )
        }
    }

    if (showLogoutSheet.value) {
        ConfirmationBottomSheet(
            title = "Logout Akun",
            description = "Apakah Anda yakin ingin logout akun dari aplikasi?",
            iconResId = R.drawable.ilt_logout,
            sheetState = sheetState,
            onDismiss = { showLogoutSheet.value = false },
            onFirstButton = {
                showLogoutSheet.value = false
                authViewModel.signout()
                rootNavController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                }

            },
            onSecondButton = {
                showLogoutSheet.value = false
            },
            firstText = "Ya, saya yakin",
            secondText = "Tidak"
        )
    }
    if (showDeleteAbsentSheet.value) {
        ConfirmationBottomSheet(
            title = "Hapus Riwayat Absensi",
            description = "Apakah Anda yakin ingin menghapus semua riwayat absensi?",
            iconResId = R.drawable.ilt_scared,
            sheetState = sheetState,
            onDismiss = { showLogoutSheet.value = false },
            onFirstButton = {

                authViewModel.deleteAllAbsenceHistory()
                showDeleteAbsentSheet.value = false



            },
            onSecondButton = {
                showDeleteAbsentSheet.value = false
            },
            firstText = "Ya, saya yakin",
            secondText = "Tidak"
        )
    }



}


