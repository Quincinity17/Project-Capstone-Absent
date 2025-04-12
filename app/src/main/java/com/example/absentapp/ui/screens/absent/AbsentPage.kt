package com.example.absentapp.ui.screens.absent

// Android & Java Standard
import android.content.Context
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.Log
import android.view.View
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

// Jetpack Compose - Foundation
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

// Jetpack Compose - Material
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// Jetpack Compose - Runtime & UI
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

// Navigation
import androidx.navigation.NavController

// Project-specific
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.data.dataStore.JadwalCachePreference
import com.example.absentapp.data.model.Jadwal
import com.example.absentapp.location.LocationService
import com.example.absentapp.location.LocationViewModel

// Coroutine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AbsenPage(
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val jadwalPrefs = remember { JadwalCachePreference(context) }
    val coroutineScope = rememberCoroutineScope()

    val absenTime by authViewModel.absenTime.collectAsState()
    val currentEmail = authViewModel.getCurrentUserEmail()
    val locationText by locationViewModel.location.collectAsState()
    val distance by locationViewModel.currentDistance.collectAsState()
    val isLoadingLokasi by locationViewModel.isFetchingLocation.collectAsState()


    var jadwalHariIni by remember { mutableStateOf<Jadwal?>(null) }

    // Step 1: Load from cache
    LaunchedEffect(Unit) {
        jadwalPrefs.cachedJadwal.collect { cached ->
            if (cached != null) jadwalHariIni = cached
        }
    }

    // Step 2: Fetch from Firestore and update cache
    LaunchedEffect(Unit) {
        authViewModel.getTodaySchedule { fetched ->
            fetched?.let {
                jadwalHariIni = it
                coroutineScope.launch {
                    jadwalPrefs.saveJadwal(it)
                }
            }
        }
    }

    val now = Calendar.getInstance()
    val startOfDay = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val endOfDay = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    val distanceLimit by locationViewModel.distanceLimit.collectAsState()


    val filteredAbsen = absenTime
        .filter { it.name == currentEmail }
        .filter {
            val time = it.timestamp?.toDate()
            time != null && time.after(startOfDay.time) && time.before(endOfDay.time)
        }
        .sortedBy { it.timestamp?.toDate() }

    val sudahAbsenPulang = remember(filteredAbsen, jadwalHariIni) {
        val thresholdPulang = jadwalHariIni?.jamMasuk?.let {
            runCatching {
                LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                    .plusHours(3) // anggap 3 jam setelah masuk itu waktu pulang
            }.getOrNull()
        }

        filteredAbsen.any { absen ->
            val absenTime = absen.timestamp?.toDate()?.toInstant()
                ?.atZone(java.time.ZoneId.systemDefault())?.toLocalTime()

            absenTime != null && thresholdPulang != null && absenTime.isAfter(thresholdPulang)
        }
    }


    val sudahAbsen = filteredAbsen.isNotEmpty()
    val jamPulang = jadwalHariIni?.jamKeluar ?: "--:--"
    val jamMasukBesok = jadwalHariIni?.jamMasuk ?: "--:--"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_gradient),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth()
            )

            AbsenGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.BottomCenter),
                currentTime = LocalDateTime.now(),
                enabled = distance <= distanceLimit,
                onClickAbsen = {
                    navController.navigate("camera")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))



        AbsenStatusBanner(
            isAbsenHariIni = sudahAbsen,
            isSedangMengambilLokasi = isLoadingLokasi,
            sudahAbsenPulang = sudahAbsenPulang, // hasil dari filter absen
            distance = distance,
            jadwalMasuk = jadwalHariIni?.jamMasuk ?: "--:--",
            jadwalPulang = jamPulang,
            jamMasukBesok = jamMasukBesok,
            distanceLimit = distanceLimit, // ⬅️ ini dia

        )


        Spacer(modifier = Modifier.height(16.dp))

        jadwalHariIni?.let {
            Text(
                text = "Jadwal Anda hari ini ya :)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            JadwalCard(
                icon = R.drawable.ic_check,
                label = "masuk",
                waktu = it.jamMasuk
            )

            JadwalCard(
                icon = R.drawable.ic_car,
                label = "pulang",
                waktu = it.jamKeluar
            )
        } ?: Text(
            "Jadwal hari ini tidak ditemukan",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AbsenGlassCard(
    modifier: Modifier = Modifier,
    currentTime: LocalDateTime,
    onClickAbsen: () -> Unit,
    enabled: Boolean = true

) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("id"))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp)
            )

    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_glass),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .padding(24.dp)
                .wrapContentHeight(),

                    horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically

            ) {
                Image(
                    painter = painterResource(R.drawable.ilt_clock),
                    contentDescription = "Clock Illustration",
                    modifier = Modifier.size(80.dp)
                )
                Column (
                    modifier = Modifier.padding(start = 12.dp)

                ){
                    Text(
                        text = currentTime.format(timeFormatter),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text(
                        text = currentTime.format(dateFormatter),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                enabled = enabled, // ⬅️ tambahkan ini
                onClick = onClickAbsen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF022D9B)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Presensi Sekarang",
                        fontSize = 21.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(R.drawable.ic_enter),
                        contentDescription = "Face Icon",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun JadwalCard(
    icon: Int,
    label: String,
    waktu: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 4.dp)
            .background(Color(0xFFF9F9F9), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF50C2C9), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row {
                    Text("Jadwal ", color = Color.Black)
                    Text(label, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(" Anda hari ini", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = waktu,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF50C2C9)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AbsenStatusBanner(
    isAbsenHariIni: Boolean,
    isSedangMengambilLokasi: Boolean,
    sudahAbsenPulang: Boolean,
    distance: Float,
    distanceLimit: Int, // ⬅️ Tambahkan ini
    jadwalMasuk: String,
    jadwalPulang: String,
    jamMasukBesok: String
) {
    val now = LocalTime.now()
    val masukTime = runCatching {
        LocalTime.parse(jadwalMasuk, DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull()

    val pulangTime = runCatching {
        LocalTime.parse(jadwalPulang, DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull()

    var color = Color.Gray
    val message = buildAnnotatedString {
        when {
            isSedangMengambilLokasi -> {
                color = Color(0xFF757575) // abu-abu netral
                append("Sedang mengambil data lokasi...")
            }

            // Belum masuk jam masuk
            masukTime != null && now.isBefore(masukTime) -> {
                // Warna sesuai apakah user dalam zona absensi
                color = if (distance <= distanceLimit) Color(0xFF4CAF50) else Color(0xFFB98800)

                appendLine("Anda belum absen hari ini.")

                if (distance <= distanceLimit) {
                    appendLine("Anda sudah berada di zona absensi.")
                } else {
                    val distanceGap = (distance - distanceLimit).toInt().coerceAtLeast(1)
                    appendLine("Anda tidak berada di zona absensi.")
                    appendLine("Mendekatlah hingga ${distanceGap}m lagi.")
                }

                // Jarak ditampilkan tebal
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("(jarak ${distance.toInt()}m)")
                }

                // Info tambahan jika user sudah absen masuk
                if (isAbsenHariIni) {
                    append("\n\n")
                    color = Color(0xFF01CCAD)
                    append("Jangan lupa absen pulang Anda, pada pukul ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(jadwalPulang)
                    }
                }
            }

// Sudah masuk tapi belum pulang
            masukTime != null && pulangTime != null &&
                    now.isAfter(masukTime) && now.isBefore(pulangTime) -> {

                // Warna sesuai zona
                color = if (distance <= distanceLimit) Color(0xFFEF5350) else Color(0xFFFF7043)

                appendLine("Anda belum absen hari ini.")

                if (distance <= distanceLimit) {
                    appendLine("Anda sudah berada di zona absensi.")
                } else {
                    val distanceGap = (distance - distanceLimit).toInt().coerceAtLeast(1)
                    appendLine("Anda tidak berada di zona absensi.")
                    appendLine("Mendekatlah hingga ${distanceGap}m lagi.")
                }

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("(jarak ${distance.toInt()}m)")
                }

                // Jika sudah absen masuk
                if (isAbsenHariIni) {
                    append("\n\n")
                    color = Color(0xFF01CCAD)
                    append("Jangan lupa absen pulang, pada pukul ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(jadwalPulang)
                    }
                }
            }

// Sudah lewat jam pulang
            masukTime != null && pulangTime != null &&
                    now.isAfter(pulangTime) -> {

                if (!sudahAbsenPulang) {
                    color = if (distance <= distanceLimit) Color(0xFFEF6C00) else Color(0xFFFF7043)

                    append("Jangan lupa absen pulang Anda pada pukul ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("$jadwalPulang\n")
                    }

                    if (distance <= distanceLimit) {
                        appendLine("Anda sudah berada di zona absensi.")
                    } else {
                        val distanceGap = (distance - distanceLimit).toInt().coerceAtLeast(1)
                        appendLine("Anda tidak berada di zona absensi.")
                        appendLine("Mendekatlah hingga ${distanceGap}m lagi.")
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("(jarak ${distance.toInt()}m)")
                    }
                } else {
                    // Jika sudah absen pulang, beri ucapan istirahat dan jadwal besok
                    color = Color(0xFF388E3C) // hijau adem
                    append("Selamat beristirahat, jangan lupa absen besok pada pukul ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(jamMasukBesok)
                    }
                }
            }
            else -> {
                color = Color.Gray
                append("Status absensi tidak diketahui.")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = color,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

