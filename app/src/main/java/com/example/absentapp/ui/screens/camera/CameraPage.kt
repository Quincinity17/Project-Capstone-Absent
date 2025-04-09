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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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
    navController: NavController
) {
    val bitmaps by viewModel.bitmaps.collectAsState()
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var simulateFlash by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        try {
            controller.bindToLifecycle(lifecycleOwner)
            controller.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        } catch (e: Exception) {
            Log.e("CameraPage", "\u274c Error binding camera: ${e.message}", e)
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
            CameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize()
            )

            if (simulateFlash) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White))
            }

            ViewfinderOverlay(
                widthPercent = 0.7f,
                heightPercent = 0.6f,
                verticalOffsetPercent = -0.05f
            )

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

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
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
                                val isFront = controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
                                if (isFront && flashEnabled) {
                                    simulateFlash = true
                                    delay(150)
                                }
                                takePhoto { bitmap ->
                                    previewBitmap = bitmap
                                    showPreviewDialog = true
                                    simulateFlash = false
                                    if (!isFront && flashEnabled) {
                                        controller.cameraControl?.enableTorch(false)
                                    }
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

            if (showPreviewDialog && previewBitmap != null) {
                AlertDialog(
                    onDismissRequest = { showPreviewDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.onTakePhoto(previewBitmap!!)
                            authViewModel.absenWithPhoto(previewBitmap!!, context)
                            showPreviewDialog = false
                            navController.navigate("home") {
                                popUpTo("camera") { inclusive = true }
                            }
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPreviewDialog = false }) {
                            Text("Retake")
                        }
                    },
                    text = {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Captured photo",
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        )
                    }
                )
            }
        }
    }
}


@Composable
fun ViewfinderOverlay(
    modifier: Modifier = Modifier,
    widthPercent: Float,
    heightPercent: Float,
    verticalOffsetPercent: Float = 0f,
    cornerRadius: Dp = 24.dp
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasWidth = with(LocalDensity.current) { maxWidth.toPx() }
        val canvasHeight = with(LocalDensity.current) { maxHeight.toPx() }
        val cornerRadiusPx = with(LocalDensity.current) { cornerRadius.toPx() }

        val holeWidthPx = canvasWidth * widthPercent
        val holeHeightPx = canvasHeight * heightPercent
        val verticalOffsetPx = canvasHeight * verticalOffsetPercent

        val left = (canvasWidth - holeWidthPx) / 2f
        val top = (canvasHeight - holeHeightPx) / 2f + verticalOffsetPx

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color.Black.copy(alpha = 0.5f))
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(holeWidthPx, holeHeightPx),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                blendMode = BlendMode.Clear
            )
        }
    }
}


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
        Spacer(modifier = Modifier.width(56.dp)) // Spacer biar tetap seimbang

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



//@Preview(showBackground = true)
//@Composable
//fun CameraBottomBarPreview() {
//    Box(modifier = Modifier.background(Color.Transparent)) {
//        CameraBottomBar(
//            onTakePhoto = {},
//            onSwitchCamera = {},
//        )
//    }
//}
