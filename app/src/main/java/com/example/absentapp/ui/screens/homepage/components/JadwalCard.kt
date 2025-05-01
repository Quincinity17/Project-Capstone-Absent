package com.example.absentapp.ui.screens.homepage.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absentapp.ui.theme.LocalAppColors

/**
 * Komponen kartu jadwal yang menampilkan informasi jam masuk/pulang dengan ikon dan label.
 *
 * @param icon ID resource ikon yang ingin ditampilkan.
 * @param label Label jadwal, misalnya "Masuk" atau "Pulang".
 * @param waktu Jam yang ditampilkan, misalnya "07:00".
 */
@Composable
fun JadwalCard(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    waktu: String
) {
    val appColors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(appColors.secondaryBackground, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .clearAndSetSemantics {
                // Untuk pembaca layar (TalkBack)
                contentDescription = "Jadwal $label Anda hari ini pukul $waktu"
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Ikon di kiri dalam kotak warna biru
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF50C2C9), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = appColors.secondaryBackground,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                // Baris teks label
                Row {
                    Text("Jadwal ", color = appColors.primaryText)
                    Text(label, fontWeight = FontWeight.Bold, color = appColors.primaryText)
                    Text(" Anda hari ini", color = appColors.primaryText)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Teks jam/waktu yang ditampilkan
                Text(
                    text = waktu,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = appColors.primaryText
                )
            }
        }
    }
}
