package com.example.absentapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.ui.platform.LocalContext
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.ui.screens.auth.LoginPage
import com.example.absentapp.ui.screens.auth.SignupPage
import com.example.absentapp.ui.screens.camera.CameraPage
import com.example.absentapp.ui.screens.camera.CameraViewModel
import com.example.absentapp.ui.screens.main.MainScreen
import androidx.camera.view.CameraController
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.content.ContextCompat
import com.example.absentapp.ui.screens.splash.SplashPage

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationHost(
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
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashPage(navController = navController, authViewModel = authViewModel)
        }
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage( navController, authViewModel)
        }
        composable("main") {
            MainScreen( authViewModel, locationViewModel, navController)
        }
        composable("camera") {
            CameraPage(
                controller = controller,
                viewModel = cameraViewModel,
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
                                cameraViewModel.setCameraError("Gagal mengambil foto. Coba lagi.")
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
