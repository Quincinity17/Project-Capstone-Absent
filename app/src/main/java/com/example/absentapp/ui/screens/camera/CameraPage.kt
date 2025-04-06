package com.example.absentapp.ui.screens.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.absentapp.R
import com.example.absentapp.auth.AuthState
import com.example.absentapp.auth.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPage(
    controller: LifecycleCameraController,
    viewModel: CameraViewModel,
    scaffoldState: BottomSheetScaffoldState,
    scope: CoroutineScope,
    takePhoto: (onPhotoTaken: (Bitmap) -> Unit) -> Unit,
    authViewModel: AuthViewModel,
    navController: NavController // ⬅️ ditambahkan
) {
    val bitmaps by viewModel.bitmaps.collectAsState()
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // ✅ Observe auth state untuk navigasi
    val authState by authViewModel.authState.observeAsState()

    // ✅ Navigasi otomatis setelah absen sukses
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            Log.d("Klepon", "✅ Absen sukses: ${authState}")
            navController.navigate("home") {
                popUpTo("camera") { inclusive = true }
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            BoxWithConstraints {
                PhotoBottomSheetContent(
                    bitmaps = bitmaps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxHeight * 0.5f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Preview Kamera
            CameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize()
            )

            // Tombol switch kamera
            IconButton(
                onClick = {
                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else CameraSelector.DEFAULT_BACK_CAMERA
                },
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.TopStart)
                    .size(72.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_switch_camera),
                    contentDescription = "Switch Camera",
                    tint = Color.Unspecified
                )
            }

            // Tombol Kamera & Galeri
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        takePhoto { bitmap ->
                            previewBitmap = bitmap
                            showPreviewDialog = true
                            Log.d("Klepon", "📸 Tombol kamera ditekan")
                            authViewModel.absenWithPhoto(bitmap, context)
                        }
                    },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Take Photo",
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { scope.launch { scaffoldState.bottomSheetState.expand() } },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_galery),
                        contentDescription = "Open Gallery"
                    )
                }
            }

            // 📸 Popup Preview Foto
            if (showPreviewDialog && previewBitmap != null) {
                AlertDialog(
                    onDismissRequest = { showPreviewDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.onTakePhoto(previewBitmap!!)
                            showPreviewDialog = false
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showPreviewDialog = false
                        }) {
                            Text("Retake")
                        }
                    },
                    text = {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Captured photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                )
            }
        }
    }
}
