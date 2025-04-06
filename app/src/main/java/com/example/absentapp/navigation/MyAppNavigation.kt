package com.example.absentapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.ui.platform.LocalContext
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.ui.screens.auth.LoginPage
import com.example.absentapp.ui.screens.auth.SignupPage
import com.example.absentapp.ui.screens.camera.CameraPage
import com.example.absentapp.ui.screens.camera.CameraViewModel
import com.example.absentapp.ui.screens.home.Homepage
import androidx.camera.view.CameraController
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    cameraViewModel: CameraViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val controller = LifecycleCameraController(context).apply {
        setEnabledUseCases(
            CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
        )
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home") {
            Homepage(modifier, navController, authViewModel, locationViewModel)
        }
        composable("camera") {
            CameraPage(
                controller = controller,
                viewModel = cameraViewModel,
                scaffoldState = scaffoldState,
                scope = scope,
                takePhoto = { onPhotoTaken ->
                    controller.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val matrix = android.graphics.Matrix().apply {
                                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                                    if (controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                        postScale(-1f, 1f)
                                    }
                                }

                                val bitmap = Bitmap.createBitmap(
                                    image.toBitmap(),
                                    0,
                                    0,
                                    image.width,
                                    image.height,
                                    matrix,
                                    true
                                )
                                onPhotoTaken(bitmap)
                                image.close()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                // Optional: log error
                            }
                        }
                    )
                },
                authViewModel = authViewModel,
                navController = navController
            )
        }

    }
}
