package com.example.absentapp.ui.screens.riwayat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absentapp.R
import com.example.absentapp.ui.theme.LocalAppColors

@Composable
fun RiwayatCard(
    timenote: String,
    date: String,
    hourMinute: String,
    type: String,
    onPhotoClick: (() -> Unit)? = null
) {
    val darkTextColor = Color(0xFF2D1D1D)
    val appColors = LocalAppColors.current

    val titleText = if (type.lowercase() == "keluar") "Checkout Time" else "Checkin Time"
    val iconRes = if (type.lowercase() == "keluar") R.drawable.ic_arrow_circle_left else R.drawable.ic_arrow_circle_right
    val iconColor = if (type.lowercase() == "keluar") appColors.iconColorCheckOut else appColors.iconColorCheckIn
    val iconBgColor = if (type.lowercase() == "keluar") appColors.iconBgColorCheckOut else appColors.iconBgColorCheckIn






    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.secondaryBackground),
        border = BorderStroke(1.dp, appColors.strokeButton)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Bagian atas (dibungkus Box untuk rounded background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    .background(appColors.primaryBackground)
                    .border(
                        width = 1.dp,
                        color = appColors.strokeButton,
                        shape = RoundedCornerShape(12.dp)

                    )
                    .clip(RoundedCornerShape(16.dp))

            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
                        .clearAndSetSemantics {
                            contentDescription = "Telah absen $type dan $timenote di $date pada pukul $hourMinute "
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )

                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = titleText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.primaryText
                            )
                            Text(
                                text = timenote,
                                fontSize = 12.sp,
                                color = appColors.secondaryText
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = date,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors.secondaryText
                        )
                        Text(
                            text = hourMinute,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.primaryText
                        )
                    }
                }
            }



            // Tombol bawah
            if (onPhotoClick != null) {
                TextButton(
                    onClick = onPhotoClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clearAndSetSemantics {
                            contentDescription = "Lihat foto bukti $type di $date jam $hourMinute"
                        },
                    colors = ButtonDefaults.textButtonColors(contentColor = darkTextColor)
                ) {
                    Text(
                        text = "Lihat Foto Bukti",
                        fontSize = 14.sp,
                        color = appColors.primaryText,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f) // Biar teks dorong icon ke kanan
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = appColors.primaryText
                    )
                }

            }
        }
    }
}






@Preview(showBackground = true)
@Composable
fun PreviewRiwayatCard() {
    MaterialTheme {
        Surface {
            RiwayatCard(
                timenote = "Tepat Waktu",
                date = "Hari ini",
                hourMinute = "08:00",
                type = "masuk",
                onPhotoClick = {}
            )
        }
    }
}
