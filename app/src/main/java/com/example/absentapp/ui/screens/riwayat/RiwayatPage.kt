package com.example.absentapp.ui.screens.riwayat

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.ui.components.Popup
import com.example.absentapp.ui.screens.riwayat.components.RiwayatCard
import com.example.absentapp.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * RiwayatPage.kt
 * Halaman Riwayat untuk menampilkan daftar presensi (check-in dan check-out) user.
 *
 * Fitur:
 * - Menampilkan daftar riwayat absen berdasarkan email user yang sedang login
 * - Menampilkan status waktu absen (tepat waktu, telat, lebih cepat)
 * - Menampilkan popup foto bukti absen jika tersedia
 * - Aksesibilitas untuk TalkBack pengguna disabilitas visual
 *
 * @param fromBottomBar Menandakan apakah halaman ini dipanggil dari bottom navigation
 * @param authViewModel ViewModel untuk autentikasi dan data user
 */

@Composable
fun RiwayatPage(
    fromBottomBar: Boolean,
    authViewModel: AuthViewModel
) {
    val absenTime by authViewModel.absenTime.collectAsState()
    val currentEmail = authViewModel.getCurrentUserEmail()
    var selectedPhoto by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val appColors = LocalAppColors.current

    // Fokus TalkBack diarahkan ke heading jika dipanggil dari BottomNavigation
    LaunchedEffect(fromBottomBar) {
        if (fromBottomBar) {
            snapshotFlow { true }.first()
            delay(150)
            focusRequester.requestFocus()
        }
    }

    // Jika belum login, tampilkan pesan
    if (currentEmail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User belum login")
        }
        return
    }

    // Filter daftar absen milik user yang sedang login
    val userAbsens = absenTime
        .filter { it.name == currentEmail }
        .sortedByDescending { it.timestamp?.toDate() }

    // Layout utama
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
            .background(appColors.primaryBackground)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .semantics(mergeDescendants = true) {
                isTraversalGroup = true
                traversalIndex = 1f
            },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Halaman Riwayat",
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .focusable()
                .semantics {
                    contentDescription = "Halaman Riwayat Absensi"
                    heading()
                },
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = appColors.primaryText,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Jika belum ada data absen
        if (userAbsens.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp)
                    .semantics {
                        contentDescription = "Anda belum ada data absen. Silakan lakukan presensi terlebih dahulu"
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        color = appColors.secondaryText
                    )
                }
            }
        } else {
            // Menampilkan setiap entri absen
            userAbsens.forEach { absen ->
                val timeStamp = absen.timestamp?.toDate()

                // Format jam dan tanggal
                val hourMinute = timeStamp?.let {
                    android.text.format.DateFormat.format("HH:mm", it).toString()
                } ?: "--:--"

                val date = timeStamp?.let {
                    val now = Calendar.getInstance()
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

    // Menampilkan popup jika user klik tombol "Lihat Foto Bukti"
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

/**
 * Format teks timeNote sesuai jenis absen (masuk/pulang).
 */
fun formatTimeNoteByType(timeNote: String?, type: String?): String {
    if (timeNote.isNullOrBlank()) return ""
    return when (type) {
        "masuk" -> formatTimeNote(timeNote)
        "keluar" -> timeNote
        else -> timeNote
    }
}

/**
 * Mengubah string waktu seperti "+15" menjadi "Lebih cepat 15 menit"
 * atau "-70" menjadi "Telat 1 jam 10 menit"
 */
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
