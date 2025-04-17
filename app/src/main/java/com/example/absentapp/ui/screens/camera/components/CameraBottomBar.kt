package com.example.absentapp.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.absentapp.R

@Composable
fun CameraBottomBar(
    onTakePhoto: () -> Unit,
    onSwitchCamera: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(56.dp))

        // Tengah: Tombol Ambil Foto
        IconButton(
            onClick = onTakePhoto,
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(42.dp))
                .background(Color.White)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_circle),
                contentDescription = "Take Photo",
                modifier = Modifier.size(80.dp),
                tint = Color.Black
            )
        }

        // Kanan: Tombol Switch Kamera
        IconButton(
            onClick = onSwitchCamera,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_switch),
                contentDescription = "Switch Camera",
                tint = Color.White
            )
        }
    }
}