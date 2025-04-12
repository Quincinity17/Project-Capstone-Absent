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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ProfilePage(authViewModel: AuthViewModel, navController: NavController) {
    val absenTime by authViewModel.absenTime.collectAsState()
    val currentEmail = authViewModel.getCurrentUserEmail()
    var selectedPhoto by remember { mutableStateOf<String?>(null) }

    if (currentEmail == null) {
        // User belum login
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("User belum login")
        }
    } else {
        val userAbsens = absenTime
            .filter { it.name == currentEmail }
            .sortedByDescending { it.timestamp?.toDate() }

        if (userAbsens.isEmpty()) {
            // ⬇️ Layout untuk keadaan kosong → tanpa scroll, center 100%
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.ilt_empty),
                        contentDescription = "Ilustrasi tidak ada data",
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Belum ada data absen",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Silakan lakukan presensi terlebih dahulu untuk melihat riwayat.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // ⬇️ Layout saat ada data → pakai scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                userAbsens.forEach { absen ->
                    val timeStamp = absen.timestamp?.toDate()

                    val hourMinute = timeStamp?.let {
                        android.text.format.DateFormat.format("HH:mm", it).toString()
                    } ?: "--:--"

                    val date = if (timeStamp != null) {
                        val cal = Calendar.getInstance()
                        val now = cal.clone() as Calendar

                        val then = Calendar.getInstance().apply { time = timeStamp }

                        val isToday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

                        now.add(Calendar.DAY_OF_YEAR, -1)
                        val isYesterday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

                        when {
                            isToday -> "Hari ini"
                            isYesterday -> "Kemarin"
                            else -> SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(timeStamp)
                        }
                    } else {
                        "-"
                    }

                    AbsenCard(
                        timenote = formatTimeNoteByType(absen.timeNote, absen.type),
                        type = absen.type ?: "-",
                        date = date,
                        hourMinute = hourMinute,
                        onPhotoClick = absen.photoBase64?.let {
                            { selectedPhoto = it }
                        }
                    )
                }
            }
        }
    }


//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        if (currentEmail == null) {
//            Text("User belum login")
//        } else {
//            val userAbsens = absenTime
//                .filter { it.name == currentEmail }
//                .sortedByDescending { it.timestamp?.toDate() }
//
//            if (userAbsens.isEmpty()) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(32.dp), // padding di luar column
//                    contentAlignment = Alignment.Center
//                ) {
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Image(
//                            painter = painterResource(R.drawable.ilt_empty),
//                            contentDescription = "Ilustrasi tidak ada data",
//                            modifier = Modifier.size(200.dp)
//                        )
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        Text(
//                            text = "Belum ada data absen",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Gray
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        Text(
//                            text = "Silakan lakukan presensi terlebih dahulu untuk melihat riwayat.",
//                            fontSize = 14.sp,
//                            color = Color.Gray
//                        )
//                    }
//                }
//            }
//            else {
//                userAbsens.forEach { absen ->
//                    val timeStamp = absen.timestamp?.toDate()
//
//                    val hourMinute = timeStamp?.let {
//                        android.text.format.DateFormat.format("HH:mm", it).toString()
//                    } ?: "--:--"
//
//                    val date = if (timeStamp != null) {
//                        val cal = Calendar.getInstance()
//                        val now = cal.clone() as Calendar
//
//                        val then = Calendar.getInstance().apply { time = timeStamp }
//
//                        val isToday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
//                                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
//
//                        now.add(Calendar.DAY_OF_YEAR, -1)
//                        val isYesterday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
//                                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
//
//                        when {
//                            isToday -> "Hari ini"
//                            isYesterday -> "Kemarin"
//                            else -> SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(timeStamp)
//                        }
//                    } else {
//                        "-"
//                    }
//
//
//                    AbsenCard(
//                        timenote = formatTimeNoteByType(absen.timeNote, absen.type),
//                        type = absen.type ?: "-",
//                        date = date,
//                        hourMinute = hourMinute,
//                        onPhotoClick = absen.photoBase64?.let {
//                            { selectedPhoto = it }
//                        }
//                    )
//                }
//
//            }
//        }
//    }

    if (selectedPhoto != null) {
        Dialog(onDismissRequest = { selectedPhoto = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .width(300.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bukti Kehadiran",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedPhoto = null }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close_filled),
                                contentDescription = "Tutup",
                                tint = Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }


                    Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))


                    // Foto
                    val decodedBytes = Base64.decode(selectedPhoto, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Foto Absen",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f) // bikin kotak
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.LightGray)
                    )
                }
            }
        }
    }

}

fun formatTimeNoteByType(timeNote: String?, type: String?): String {
    if (timeNote.isNullOrBlank()) return ""

    return when (type) {
        "masuk" -> formatTimeNote(timeNote)
        "keluar" -> timeNote ?: "" // langsung tampilkan, sudah dalam bentuk "Hadir selama ..."
        else -> timeNote
    }
}

fun formatTimeNote(timeNote: String?): String {
    if (timeNote.isNullOrBlank()) return ""

    val trimmed = timeNote.trim()
    val isLate = trimmed.startsWith("-")
    val isEarly = trimmed.startsWith("+")

    // Ambil angka menitnya
    val minutes = trimmed.drop(1).trim().toIntOrNull() ?: return ""

    val hours = minutes / 60
    val remainingMinutes = minutes % 60

    val waktu = buildString {
        if (hours > 0) append("$hours jam ")
        if (remainingMinutes > 0 || hours == 0) append("$remainingMinutes menit")
    }.trim()

    return when {
        isLate -> "Telat $waktu"
        isEarly -> "Lebih cepat $waktu"
        else -> waktu // fallback
    }
}


