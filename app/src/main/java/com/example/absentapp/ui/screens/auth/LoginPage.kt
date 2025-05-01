package com.example.absentapp.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.absentapp.auth.AuthState
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.ui.components.CustomTextField
import com.example.absentapp.ui.theme.LocalAppColors

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    // Efek samping saat status autentikasi berubah
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("main")
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    // Tampilan utama login
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0x4D034B9F),
                        0.1f to appColors.primaryBackground,
                        0.9f to appColors.primaryBackground,
                        1.0f to Color(0x4D01CBAE)
                    )
                )
            )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Judul dan deskripsi
            Text(text = "Masuk", color = appColors.primaryText, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Silakan masukkan email dan kata sandi",
                color = appColors.secondaryText,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Field email
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Field password
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                isPassword = true,
                placeholder = "Password",
                modifier = Modifier.focusRequester(passwordFocusRequester),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Login
            Button(
                onClick = { authViewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = if (email.isNotBlank() && password.isNotBlank()) {
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF024494), Color(0xFF009285))
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    appColors.gradientDisabledButtonStart,
                                    appColors.gradientDisabledButtonEnd
                                )
                            )
                        }
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                contentPadding = ButtonDefaults.ContentPadding,
                enabled = authState.value != AuthState.Loading && email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Login", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol untuk ke halaman signup
            TextButton(onClick = { navController.navigate("signup") }) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = appColors.secondaryText)) {
                            append("Apakah belum punya akun? ")
                        }
                        withStyle(style = SpanStyle(color = appColors.primaryButtonColors)) {
                            append("Sign up")
                        }
                    }
                )
            }
        }
    }
}
