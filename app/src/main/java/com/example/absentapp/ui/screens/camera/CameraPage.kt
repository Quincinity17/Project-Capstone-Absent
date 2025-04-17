package com.example.absentapp.ui.screens.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.ui.screens.camera.components.CameraBottomBar
import com.example.absentapp.ui.screens.camera.components.CameraErrorDialog
import com.example.absentapp.ui.screens.camera.components.CameraPreview
import com.example.absentapp.ui.screens.camera.components.CameraPreviewDialog
import com.example.absentapp.ui.screens.camera.components.ViewfinderOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Halaman utama kamera yang digunakan untuk mengambil selfie sebagai bukti absensi.
 * Menampilkan preview kamera, tombol kendali, overlay viewfinder, dan galeri foto hasil selfie.
 *
 * @param controller CameraX lifecycle-aware controller untuk preview dan pengambilan gambar.
 * @param viewModel ViewModel untuk menyimpan hasil bitmap dan mengatur error state.
 * @param scaffoldState Scaffold state untuk mengatur bottom sheet foto.
 * @param scope CoroutineScope untuk menjalankan logika async (pengambilan gambar).
 * @param takePhoto Callback yang akan dipanggil ketika tombol ambil foto ditekan.
 * @param authViewModel ViewModel yang digunakan untuk mengirim data absen (foto).
 * @param navController NavController untuk navigasi ke halaman lain.
 */
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


    // Tampilkan dialog error jika gagal ambil foto
    errorMessage?.let {
        CameraErrorDialog(message = it) {
            viewModel.clearError()
            navController.navigate("camera") {
                popUpTo("camera") { inclusive = true }
            }
        }
    }

    // Bind CameraX ke lifecycle
    LaunchedEffect(Unit) {
        try {
            controller.bindToLifecycle(lifecycleOwner)
            controller.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        } catch (e: Exception) {
            Log.e("CameraPage", "Error binding camera: ${e.message}", e)
        }
    }

    // UI utama kamera
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            controller = controller,
            modifier = Modifier.fillMaxSize()
        )

        ViewfinderOverlay(
            widthPercent = 0.7f,
            heightPercent = 0.6f,
            verticalOffsetPercent = -0.05f
        )

        // Tombol back
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .size(64.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Info & tombol kendali
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
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

        // Dialog preview foto
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
                    onRetake = { showPreviewDialog = false }
                )
            }
        }
    }
}

