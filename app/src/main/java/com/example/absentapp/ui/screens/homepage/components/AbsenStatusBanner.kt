package com.example.absentapp.ui.screens.homepage.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absentapp.R
import com.example.absentapp.ui.theme.LocalAppColors
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AbsenStatusBanner(
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
    val appColors = LocalAppColors.current

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
            isUpdateData ->{
                color = Color(0xFF757575) // abu-abu netral
                append("Sedang mengupdate data ...")
            }

            isSedangMengambilLokasi -> {
                color = Color(0xFF757575) // abu-abu netral
                append("Sedang mengambil data lokasi...")
            }

            // Belum masuk jam masuk
            masukTime != null && now.isBefore(masukTime) -> {
                if (!isAbsenHariIni) {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )) {
                        appendLine("Anda belum absen masuk hari ini.")
                    }
                    if (distance <= distanceLimit) {
                        color = Color(0xFFE91E63)
                        appendLine("Anda sudah berada di zona absensi.")
                    } else {
                        color = Color(0xFFFF5722)
                        val distanceGap = (distance - distanceLimit).toInt().coerceAtLeast(1)
                        appendLine("Mendekatlah hingga ${distanceGap}m lagi.")
                    }

                } else if (isAbsenHariIni){
                    color = Color(0xFF01CCAD)
                    append("Jangan lupa absen pulang Anda, pada pukul ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(jadwalPulang)
                    }
                    }
                }

            // Sudah masuk tapi belum pulang
            masukTime != null && pulangTime != null && now.isAfter(masukTime) && now.isBefore(pulangTime) -> {
                if (!isAbsenHariIni) {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )) {
                        appendLine("Anda belum absen masuk hari ini.")
                    }

                    if (distance <= distanceLimit) {
                        color = Color(0xFFE91E63)
                        append("Anda sudah berada di zona absensi.")
                    } else {
                        color = Color(0xFFFF5722)
                        val distanceGap = (distance - distanceLimit).toInt().coerceAtLeast(1)
                        append("Mendekatlah hingga ${distanceGap}m lagi.")
                    }

                } else if (isAbsenHariIni){
                    color = Color(0xFF01CCAD)
                    appendLine("Jangan lupa absen pulang Anda,")
                    append("pada pukul ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(jadwalPulang)
                    }
                }
            }



// Sudah lewat jam pulang
            masukTime != null && pulangTime != null && now.isAfter(pulangTime) -> {

                if (!sudahAbsenPulang) {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )) {
                        appendLine("Anda belum absen pulang hari ini.")

                    }
                    if (distance <= distanceLimit) {
                        color = Color(0xFFE91E63)
                        appendLine("Anda sudah berada di zona absensi.")
                    } else {
                        color = Color(0xFFE91E63)
                        val distanceGap = (distance - distanceLimit).toInt().coerceAtLeast(1)
                        appendLine("Anda tidak berada di zona absensi.")
                        appendLine("Mendekatlah hingga ${distanceGap}m lagi.")
                    }
                } else if (sudahAbsenPulang){
                    color = Color(0xFF01CCAD)
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