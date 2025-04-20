package com.example.absentapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.tooling.preview.Preview
import com.example.absentapp.ui.theme.LocalAppColors


@Composable
fun Popup(
    title: String,
    onClose: () -> Unit,
    imageContent: @Composable () -> Unit,
    button1Text: String? = null,
    button2Text: String? = null,
    onButton1Click: (() -> Unit)? = null,
    onButton2Click: (() -> Unit)? = null
) {
    val appColors = LocalAppColors.current


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.secondaryBackground, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                color = appColors.primaryText
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = appColors.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image area
        Box(
            modifier = Modifier
                .fillMaxWidth(1f)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            imageContent()
        }



        // First button
        if (!button1Text.isNullOrBlank() && onButton1Click != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onButton1Click,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryButtonColors)
            ) {
                Text(button1Text, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Second button
        if (!button2Text.isNullOrBlank() && onButton2Click != null) {
            Button(
                onClick = onButton2Click,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.secondaryBackground),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(button2Text, color = appColors.primaryButtonColors)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPopup() {
    val dummyImage: @Composable () -> Unit = {
        Image(
            painter = painterResource(id = android.R.drawable.ic_dialog_info),
            contentDescription = "Contoh Gambar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )
    }

    MaterialTheme {
        Popup(
            title = "Judul Popup",
            onClose = {},
            imageContent = dummyImage,
            button1Text = "Oke",
            button2Text = "Batal",
            onButton1Click = {},
            onButton2Click = {}
        )
    }
}

