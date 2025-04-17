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
 * Dialog untuk menampilkan preview foto setelah gambar diambil.
 *
 * @param bitmap Gambar yang baru saja diambil.
 * @param onConfirm Callback saat user menekan "Confirm".
 * @param onRetake Callback saat user menekan "Retake".
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
 * Dialog untuk menampilkan pesan error saat pengambilan foto gagal.
 *
 * @param message Pesan error yang akan ditampilkan.
 * @param onRetry Callback saat user menekan "Coba Lagi".
 */
@Composable
fun CameraErrorDialog(
    message: String,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onRetry,
        confirmButton = {
            TextButton(onClick = onRetry) { Text("Coba Lagi") }
        },
        text = {
            Text(message)
        }
    )
}
