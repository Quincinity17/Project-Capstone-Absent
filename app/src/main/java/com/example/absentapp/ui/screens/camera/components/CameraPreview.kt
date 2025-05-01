package com.example.absentapp.ui.screens.camera.components

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Komponen Compose untuk menampilkan preview kamera menggunakan View interop.
 * Menggunakan LifecycleCameraController dari CameraX.
 */
@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val isBound = remember { mutableStateOf(false) }

    // Melakukan binding hanya sekali selama lifecycle hidup
    LaunchedEffect(controller) {
        if (!isBound.value) {
            controller.bindToLifecycle(lifecycleOwner)
            isBound.value = true
        }
    }

    // Preview kamera ditampilkan melalui AndroidView interop
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.controller = controller
            }
        },
        modifier = modifier
    )
}
