package com.example.absentapp.ui.screens.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.ui.screens.camera.components.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPage(
    controller: LifecycleCameraController,
    viewModel: CameraViewModel,
    scope: CoroutineScope,
    takePhoto: (onPhotoTaken: (Bitmap) -> Unit) -> Unit,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val errorMessage by viewModel.cameraError.collectAsState()
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Tampilkan dialog error jika terjadi error saat ambil foto
    errorMessage?.let {
        CameraErrorDialog(message = it) {
            viewModel.clearError()
            navController.navigate("camera") {
                popUpTo("camera") { inclusive = true }
            }
        }
    }

    // Bind CameraX controller ke lifecycle
    LaunchedEffect(Unit) {
        try {
            controller.bindToLifecycle(lifecycleOwner)
            controller.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        } catch (e: Exception) {
            Log.e("CameraPage", "Error binding camera: ${e.message}", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Tampilkan preview kamera
        CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())

        // Tampilkan overlay viewfinder transparan
        ViewfinderOverlay(
            widthPercent = 0.7f,
            heightPercent = 0.6f,
            verticalOffsetPercent = -0.05f
        )

        // Tombol kembali di kiri atas
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .size(64.dp)
                .semantics {
                    contentDescription = "Kembali ke halaman sebelumnya"
                    traversalIndex = 3f
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = null,
                tint = Color.White
            )
        }

        // Bagian bawah: Info + Tombol kamera
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .semantics {
                    isTraversalGroup = true
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Info text di atas tombol
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clearAndSetSemantics {
                            contentDescription = "Ambil selfie sebagai bukti kehadiran di lokasi"
                            traversalIndex = 0f
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ambil selfie sebagai bukti kehadiran di lokasi",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom bar tombol ambil dan switch kamera
                CameraBottomBar(
                    onTakePhoto = {
                        scope.launch {
                            takePhoto { bitmap ->
                                previewBitmap = bitmap
                                showPreviewDialog = true
                            }
                        }
                    },
                    onSwitchCamera = {
                        controller.cameraSelector =
                            if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            else CameraSelector.DEFAULT_BACK_CAMERA
                    }
                )
            }
        }

        // Dialog konfirmasi foto setelah ambil gambar
        previewBitmap?.let { bitmap ->
            if (showPreviewDialog) {
                CameraPreviewDialog(
                    bitmap = bitmap,
                    onConfirm = {
                        viewModel.onTakePhoto(bitmap)
                        authViewModel.absenWithPhoto(bitmap)
                        showPreviewDialog = false
                        navController.navigate("main") {
                            popUpTo("camera") { inclusive = true }
                        }
                    },
                    onRetake = {
                        showPreviewDialog = false
                    }
                )
            }
        }
    }
}
