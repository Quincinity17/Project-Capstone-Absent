package com.example.absentapp.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.example.absentapp.R

/**
 * Komponen bottom bar kamera yang terdiri dari:
 * - Tombol tengah untuk mengambil foto
 * - Tombol kanan untuk mengganti kamera (depan/belakang)
 */
@Composable
fun CameraBottomBar(
    onTakePhoto: () -> Unit,
    onSwitchCamera: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp), // Padding bawah untuk posisi lebih tinggi dari tepi layar
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Spacer kiri untuk memberi jarak dan simetri (tidak ada tombol di kiri)
        Spacer(modifier = Modifier.width(56.dp))

        // Tombol ambil foto di tengah (besar, fokus utama)
        IconButton(
            onClick = onTakePhoto,
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(42.dp))
                .background(Color.White)
                .semantics {
                    contentDescription = "Ambil foto"
                    traversalIndex = 1f // urutan fokus TalkBack
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_circle),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Black
            )
        }

        // Tombol untuk mengganti kamera (kanan)
        IconButton(
            onClick = onSwitchCamera,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .semantics {
                    contentDescription = "Ganti kamera depan atau belakang"
                    traversalIndex = 2f
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_switch),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
