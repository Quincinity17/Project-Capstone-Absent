package com.example.absentapp.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.absentapp.auth.AuthViewModel

@Composable
fun ProfilePage(authViewModel: AuthViewModel, navController: NavController) {
    val absenTime by authViewModel.absenTime.collectAsState()
    val currentEmail = authViewModel.getCurrentUserEmail()
    var selectedPhoto by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        if (currentEmail == null) {
            Text("User belum login")
        } else {
            val userAbsens = absenTime
                .filter { it.name == currentEmail }
                .sortedByDescending { it.timestamp?.toDate() }

            if (userAbsens.isEmpty()) {
                Text("Belum ada data absen")
            } else {
                userAbsens.forEach { absen ->
                    val time = absen.timestamp?.toDate()
                    val hourMinute = time?.let {
                        android.text.format.DateFormat.format("HH:mm", it).toString()
                    } ?: "--:--"

                    AbsenCard(
                        hourMinute = hourMinute,
                        onPhotoClick = absen.photoBase64?.let {
                            { selectedPhoto = it }
                        }
                    )
                }

            }
        }
    }

    if (selectedPhoto != null) {
        AlertDialog(
            onDismissRequest = { selectedPhoto = null },
            confirmButton = {
                TextButton(onClick = { selectedPhoto = null }) {
                    Text("Tutup")
                }
            },
            text = {
                val decodedBytes = Base64.decode(selectedPhoto, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto Absen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        )
    }
}
