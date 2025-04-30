package com.example.absentapp.ui.screens.splash

import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.auth.AuthState
import androidx.compose.runtime.livedata.observeAsState
import com.airbnb.lottie.compose.*
import androidx.compose.animation.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.absentapp.R

/**
 * Halaman Splash yang mengecek status autentikasi dan mengarahkan ke halaman yang sesuai.
 * Menampilkan animasi Lottie + teks branding aplikasi secara transisi.
 *
 * @param navController digunakan untuk navigasi ke halaman login atau main
 * @param authViewModel digunakan untuk mengecek status autentikasi pengguna
 */
@Composable
fun SplashPage(navController: NavController, authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.observeAsState()

    var delayFinished by remember { mutableStateOf(false) }
    var authChecked by remember { mutableStateOf(false) }

    // Delay awal untuk animasi splash
    LaunchedEffect(Unit) {
        delay(2500)
        delayFinished = true
        authViewModel.checkAuthState()
    }

    // Cek jika status auth sudah tersedia
    LaunchedEffect(authState) {
        if (authState != null) {
            authChecked = true
        }
    }

    // Arahkan pengguna ke halaman sesuai status login
    LaunchedEffect(delayFinished, authChecked) {
        if (delayFinished && authChecked) {
            when (authState) {
                is AuthState.Authenticated -> navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
                is AuthState.Unauthenticated -> navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
                else -> Unit
            }
        }
    }

    SplashPageUI()
}

/**
 * UI terpisah untuk halaman splash.
 * Menampilkan animasi Lottie dan teks "Absent" secara animasi.
 *
 * @param previewMode Jika true, delay animasi akan dilewati (untuk Preview Compose)
 */
@Composable
fun SplashPageUI(previewMode: Boolean = false) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anm_splash))
    val progress by animateLottieCompositionAsState(composition)
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!previewMode) delay(800)
        showText = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF009285), // gradasi biru kehijauan
                        Color(0xFF024494)  // ke biru tua
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clearAndSetSemantics {
                // Deskripsi aksesibilitas TalkBack
                "Selamat datang"
            }
        ) {
            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            AnimatedVisibility(
                visible = showText,
                enter = if (previewMode) EnterTransition.None else fadeIn() + slideInHorizontally(initialOffsetX = { -100 }),
            ) {
                Column {
                    Text(
                        text = "ATTEND",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Smart Attendance System",
                        color = Color(0xCCFFFFFF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


            }
        }
    }
}

/**
 * Preview halaman splash untuk desain
 */
@Preview(showBackground = true)
@Composable
fun SplashPagePreview() {
    SplashPageUI(previewMode = true)
}
