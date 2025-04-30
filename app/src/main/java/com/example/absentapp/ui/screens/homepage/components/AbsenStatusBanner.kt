package com.example.absentapp.ui.screens.homepage.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absentapp.R
import com.example.absentapp.ui.theme.LocalAppColors
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AbsenStatusBanner(
    modifier: Modifier = Modifier,
    isAbsenHariIni: Boolean,
    isSedangMengambilLokasi: Boolean,
    sudahAbsenPulang: Boolean,
    distance: Float,
    distanceLimit: Int,
    jadwalMasuk: String,
    jadwalPulang: String,
    jamMasukBesok: String,
    isUpdateData: Boolean
) {
    val now = LocalTime.now()

    // Parsing jadwalMasuk dan jadwalPulang dari String ke LocalTime?
<<<<<<< HEAD
    val parsedMasuk = runCatching { LocalTime.parse(jadwalMasuk) }.getOrNull()
    val parsedPulang = runCatching { LocalTime.parse(jadwalPulang) }.getOrNull()

//    Log.d("AYAMGORENG", "Masuk: $parsedMasuk | Pulang: $parsedPulang | Besok: $jamMasukBesok")
//    Log.d("AYAMGORENG", "Sebelum Masuk : ${    parsedMasuk != null && now.isBefore(parsedMasuk)}")
//    Log.d("AYAMGORENG", "Sebelum Masuk tapi belum pulang: ${    parsedMasuk != null && parsedPulang != null &&
//            now.isAfter(parsedMasuk) && now.isBefore(parsedPulang)}")
//    Log.d("AYAMGORENG", "Sudah pulang: ${    parsedPulang != null && now.isAfter(parsedPulang)}")
=======
    Log.d("AYAMGORENG", "jadwal masuk $jadwalMasuk")
    Log.d("AYAMGORENG", "jadwal pulang $jadwalPulang")
    Log.d("AYAMGORENG", "jadwal masuk bsk $jamMasukBesok")

    val parsedMasuk = runCatching { LocalTime.parse(jadwalMasuk) }.getOrNull()
    val parsedPulang = runCatching { LocalTime.parse(jadwalPulang) }.getOrNull()

    Log.d("AYAMGORENG", "Masuk: $parsedMasuk | Pulang: $parsedPulang | Besok: $jamMasukBesok")
    Log.d("AYAMGORENG", "Sebelum Masuk : ${    parsedMasuk != null && now.isBefore(parsedMasuk)}")
    Log.d("AYAMGORENG", "Sebelum Masuk tapi belum pulang: ${    parsedMasuk != null && parsedPulang != null &&
            now.isAfter(parsedMasuk) && now.isBefore(parsedPulang)}")
    Log.d("AYAMGORENG", "Sudah pulang: ${    parsedPulang != null && now.isAfter(parsedPulang)}")
>>>>>>> 6517416 (Finishing Iterasi 1)




    var color = Color.Gray
    val message = buildAnnotatedString {
        when {
            isUpdateData -> {
                color = Color(0xFF757575)
                append("Sedang mengupdate data ...")
            }

            isSedangMengambilLokasi -> {
                color = Color(0xFF757575)
                append("Sedang mengambil data lokasi...")
            }

            parsedMasuk != null && now.isBefore(parsedMasuk) -> {
                if (!isAbsenHariIni) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                        appendLine("Anda belum absen masuk hari ini.")
                    }
                    if (distance <= distanceLimit) {
                        color = Color(0xFFE91E63)
                        append("Anda sudah berada di zona absensi.")
                    } else {
                        color = Color(0xFFFF5722)
                        val gap = (distance - distanceLimit).toInt().coerceAtLeast(1)
                        append("Mendekatlah hingga ${gap}m lagi.")
                    }
                } else {
                    color = Color(0xFF01CCAD)
                    append("Jangan lupa absen pulang Anda, pada pukul ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(jadwalPulang)
                    }
                }
            }

            parsedMasuk != null && parsedPulang != null &&
                    now.isAfter(parsedMasuk) && now.isBefore(parsedPulang) -> {


                if (!isAbsenHariIni) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                        appendLine("Anda belum absen masuk hari ini.")
                    }
                    if (distance <= distanceLimit) {
                        color = Color(0xFFE91E63)
                        append("Anda sudah berada di zona absensi.")
                    } else {
                        color = Color(0xFFFF5722)
                        val gap = (distance - distanceLimit).toInt().coerceAtLeast(1)
                        append("Mendekatlah hingga ${gap}m lagi.")
                    }
                } else {
                    color = Color(0xFF01CCAD)
                    appendLine("Jangan lupa absen pulang Anda,")
                    append("pada pukul ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(jadwalPulang)
                    }
                }
            }

            parsedPulang != null && now.isAfter(parsedPulang) -> {
//                Log.d("AYAMGORENG", "Now : $now, Pulang : $parsedPulang")

                if (!sudahAbsenPulang) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                        appendLine("Anda belum absen pulang hari ini.")
                    }
                    if (distance <= distanceLimit) {
                        color = Color(0xFFE91E63)
                        append("Anda sudah berada di zona absensi.")
                    } else {
                        color = Color(0xFFE91E63)
                        val gap = (distance - distanceLimit).toInt().coerceAtLeast(1)
                        appendLine("Anda tidak berada di zona absensi.")
                        append("Mendekatlah hingga ${gap}m lagi.")
                    }
                } else {
                    color = Color(0xFF01CCAD)
                    append("Selamat beristirahat, jangan lupa absen besok pada pukul ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
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

    val plainMessage = message.text

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .clearAndSetSemantics {
                contentDescription = "Kondisi saat ini. $plainMessage"
            }
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
