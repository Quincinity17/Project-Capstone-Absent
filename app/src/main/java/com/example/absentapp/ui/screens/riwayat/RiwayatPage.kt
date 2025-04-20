package com.example.absentapp.ui.screens.riwayat

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.ui.components.Popup
import com.example.absentapp.ui.screens.riwayat.components.RiwayatCard
import com.example.absentapp.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatPage(authViewModel: AuthViewModel) {
    val absenTime by authViewModel.absenTime.collectAsState()
    val currentEmail = authViewModel.getCurrentUserEmail()
    var selectedPhoto by remember { mutableStateOf<String?>(null) }

    val appColors = LocalAppColors.current


    if (currentEmail == null) {
        // Jika belum login
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("User belum login")
        }
    } else {
        val userAbsens = absenTime
            .filter { it.name == currentEmail }
            .sortedByDescending { it.timestamp?.toDate() }




        if (userAbsens.isEmpty()) {
            // Tampilan saat tidak ada data absen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appColors.primaryBackground)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
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
                        color = appColors.primaryText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Silakan lakukan presensi terlebih dahulu",
                        fontSize = 14.sp,
                        color = appColors.secondaryText,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        } else {
            val userAbsens = absenTime
                .filter { it.name == currentEmail }
                .sortedByDescending { it.timestamp?.toDate() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(appColors.primaryBackground)
                    .padding(12.dp)
                    .padding(horizontal = 12.dp, vertical = 16.dp)

                ,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "History Page",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = appColors.primaryText,
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (userAbsens.isEmpty()) {
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
                        color = appColors.primaryText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Silakan lakukan presensi terlebih dahulu",
                        fontSize = 14.sp,
                        color = appColors.secondaryText,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    userAbsens.forEach { absen ->
                        val timeStamp = absen.timestamp?.toDate()

                        val hourMinute = timeStamp?.let {
                            android.text.format.DateFormat.format("HH:mm", it).toString()
                        } ?: "--:--"

                        val date = timeStamp?.let {
                            val cal = Calendar.getInstance()
                            val now = cal.clone() as Calendar
                            val then = Calendar.getInstance().apply { time = it }

                            val isToday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                                    now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
                            now.add(Calendar.DAY_OF_YEAR, -1)
                            val isYesterday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                                    now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

                            when {
                                isToday -> "Hari ini"
                                isYesterday -> "Kemarin"
                                else -> SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(it)
                            }
                        } ?: "-"

                        RiwayatCard(
                            timenote = formatTimeNoteByType(absen.timeNote, absen.type),
                            type = absen.type ?: "-",
                            date = date,
                            hourMinute = hourMinute,
                            onPhotoClick = absen.photoBase64?.let { { selectedPhoto = it } }
                        )
                    }
                }
            }
        }

    }

    // Dialog foto presensi
    if (selectedPhoto != null) {
        if (selectedPhoto != null) {
            Dialog(onDismissRequest = { selectedPhoto = null }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    tonalElevation = 4.dp
                ) {
                    Popup(
                        title = "Bukti Kehadiran",
                        onClose = { selectedPhoto = null },
                        imageContent = {
                            val decodedBytes = Base64.decode(selectedPhoto, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Foto Kehadiran",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
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
        "keluar" -> timeNote
        else -> timeNote
    }
}

fun formatTimeNote(timeNote: String?): String {
    if (timeNote.isNullOrBlank()) return ""
    val trimmed = timeNote.trim()
    val isLate = trimmed.startsWith("-")
    val isEarly = trimmed.startsWith("+")
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
        else -> waktu
    }
}