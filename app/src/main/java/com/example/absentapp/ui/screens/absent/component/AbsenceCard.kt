package com.example.absentapp.ui.screens.absent.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absentapp.R
import com.example.absentapp.data.model.Absence
import com.example.absentapp.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Komponen kartu absen yang menampilkan informasi pengguna, alasan, waktu, dan jumlah komentar.
 * Digunakan dalam daftar riwayat absen.
 */
@Composable
fun AbsenceCard(
    absence: Absence,
    commentCount: Int,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current

    // Format waktu dari timestamp ke bentuk: "12 Apr 2024 • 14:30"
    val formattedTime = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
        .format(Date(absence.timestamp))

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() } // Tindakan saat kartu ditekan
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        // Ikon profil di kiri
        Icon(
            painter = painterResource(R.drawable.ic_profile),
            contentDescription = "",
            tint = appColors.secondaryText,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Konten kartu
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.primaryBackground, shape = RoundedCornerShape(12.dp))
                .padding(12.dp) // agar teks tidak terlalu mepet
        ) {
            // Nama pengguna (diambil dari email)
            Text(
                text = absence.userEmail.substringBefore("@"),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = appColors.primaryText
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Alasan absen
            Text(
                text = absence.reason,
                fontSize = 14.sp,
                color = appColors.secondaryText,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Waktu dan jumlah komentar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tanggal & waktu
                Text(
                    text = formattedTime,
                    fontSize = 12.sp,
                    color = appColors.secondaryText
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Komentar (ikon + jumlah)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_comment),
                        contentDescription = "",
                        tint = appColors.secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = commentCount.toString(),
                        fontSize = 12.sp,
                        color = appColors.secondaryText
                    )
                }
            }
        }
    }
}
