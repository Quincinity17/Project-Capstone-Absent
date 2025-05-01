package com.example.absentapp.ui.screens.homepage.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absentapp.R
import com.example.absentapp.ui.theme.LocalAppColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Komponen kartu utama untuk menampilkan jam saat ini dan tombol absen.
 *
 * @param currentTime Waktu saat ini, digunakan untuk menampilkan jam dan tanggal.
 * @param onClickAbsen Fungsi callback saat tombol presensi ditekan.
 * @param enabled Apakah tombol absen dapat ditekan atau tidak.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AbsenGlassCard(
    modifier: Modifier = Modifier,
    currentTime: LocalDateTime,
    onClickAbsen: () -> Unit,
    enabled: Boolean
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("id"))

    val currentTimes = currentTime.format(timeFormatter)
    val currentDates = currentTime.format(dateFormatter)

    val appColors = LocalAppColors.current
    val isDarkMode = isSystemInDarkTheme()
    val imageRes = if (isDarkMode) R.drawable.bg_glass_dark else R.drawable.bg_glass

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(appColors.primaryBackground)
            .border(
                width = 1.dp,
                color = appColors.primaryBackground.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Latar belakang bergaya glass dengan mode gelap/terang
        Image(
            painter = painterResource(id = imageRes),
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
            // Bagian waktu dan ikon jam
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ilt_clock),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clearAndSetSemantics {}
                )

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Saat ini jam $currentTimes, $currentDates"
                        }
                ) {
                    Text(
                        text = currentTimes,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = appColors.primaryText
                    )
                    Text(
                        text = currentDates,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors.primaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol untuk melakukan presensi
            Button(
                enabled = enabled,
                onClick = onClickAbsen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.primaryButtonColors
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clearAndSetSemantics {
                        contentDescription = "Presensi sekarang"
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
