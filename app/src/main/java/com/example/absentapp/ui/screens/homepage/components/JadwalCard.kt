package com.example.absentapp.ui.screens.homepage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absentapp.ui.theme.LocalAppColors

@Composable
fun JadwalCard(
    icon: Int,
    label: String,
    waktu: String
) {
    val appColors = LocalAppColors.current


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 4.dp)
            .background(appColors.secondaryBackground, RoundedCornerShape(20.dp))
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
                    tint = appColors.secondaryBackground,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row {
                    Text("Jadwal ", color = appColors.primaryText)
                    Text(label, fontWeight = FontWeight.Bold, color = appColors.primaryText)
                    Text(" Anda hari ini", color = appColors.primaryText)
                }

                Spacer(modifier = Modifier.height(4.dp))

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