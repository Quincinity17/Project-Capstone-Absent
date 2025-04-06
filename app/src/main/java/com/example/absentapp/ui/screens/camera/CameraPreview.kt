package com.example.absentapp.ui.screens.camera

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val isBound = remember { mutableStateOf(false) }

    // Bind only once
    LaunchedEffect(controller) {
        if (!isBound.value) {
            controller.bindToLifecycle(lifecycleOwner)
            isBound.value = true
        }
    }

    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
            }
        },
        modifier = modifier
    )
}


