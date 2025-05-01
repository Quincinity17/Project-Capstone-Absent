package com.example.absentapp.ui.screens.camera.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.absentapp.ui.components.Popup

/**
 * Dialog kustom untuk menampilkan pratinjau gambar setelah pengambilan foto.
 * Menggunakan komponen `Popup` untuk tampilan visual yang konsisten.
 *
 * @param bitmap Foto yang baru diambil.
 * @param onConfirm Fungsi callback saat pengguna menekan tombol "Konfirmasi".
 * @param onRetake Fungsi callback saat pengguna ingin mengambil ulang foto.
 */
@Composable
fun CameraPreviewDialog(
    bitmap: Bitmap,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    Dialog(onDismissRequest = onRetake) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Popup(
                title = "Konfirmasi foto",
                onClose = onRetake,
                imageContent = {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Preview Foto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    )
                },
                button1Text = "Konfirmasi",
                onButton1Click = onConfirm,
                button2Text = "Ambil ulang",
                onButton2Click = onRetake
            )
        }
    }
}

/**
 * Dialog standar untuk menampilkan error saat pengambilan foto gagal.
 *
 * @param message Pesan kesalahan yang akan ditampilkan.
 * @param onRetry Fungsi callback saat pengguna menekan tombol "Coba Lagi".
 */
@Composable
fun CameraErrorDialog(
    message: String,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onRetry,
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text("Coba Lagi")
            }
        },
        text = {
            Text(message)
        }
    )
}
