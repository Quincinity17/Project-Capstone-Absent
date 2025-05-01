package com.example.absentapp.ui.screens.absent

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.ui.screens.absent.component.AbsenceCard
import com.example.absentapp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsentPage(
    fromBottomBar: Boolean,
    authViewModel: AuthViewModel,
    absenceViewModel: AbsenceViewModel,
    navController: NavHostController
) {
    val currentEmail = authViewModel.getCurrentUserEmail()
    val allAbsences by absenceViewModel.allAbsences.collectAsState()
    val commentsMap by absenceViewModel.commentsMap.collectAsState()

    val appColors = LocalAppColors.current
    val reasonText = remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
    val showBottomSheet = remember { mutableStateOf(false) }

    // Ambil semua data absensi saat pertama kali halaman dibuka
    LaunchedEffect(Unit) {
        absenceViewModel.getAllAbsences()
    }

    // Ambil komentar untuk setiap absensi
    LaunchedEffect(allAbsences) {
        allAbsences.forEach { absence ->
            absence.id?.let { absenceViewModel.loadCommentsForAbsence(it) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(appColors.primaryBackground)
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .semantics(mergeDescendants = true) {
                    isTraversalGroup = true
                    traversalIndex = 1f
                },
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Judul halaman
            Text(
                text = "Halaman Perizinan",
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Halaman Perizinan Absensi"
                        heading()
                    },
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = appColors.primaryText,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tampilkan ilustrasi jika belum ada absensi
            if (allAbsences.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.ilt_empty),
                            contentDescription = "Ilustrasi tidak ada data",
                            modifier = Modifier.size(200.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Belum ada data perizinan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.primaryText
                        )
                        Text(
                            text = "Silakan lakukan perizinan terlebih dahulu",
                            fontSize = 14.sp,
                            color = appColors.secondaryText
                        )
                    }
                }
            } else {
                // Tampilkan daftar absensi
                allAbsences
                    .sortedByDescending { it.timestamp }
                    .forEach { absence ->
                        AbsenceCard(
                            absence = absence,
                            commentCount = commentsMap[absence.id]?.size ?: 0,
                            onClick = {
                                try {
                                    absence.id?.let { id ->
                                        navController.navigate("absence_detail/$id")
                                    } ?: Log.e("PATINKUNING", "Absence ID is null")
                                } catch (e: Exception) {
                                    Log.e("PATINKUNING", "Navigation failed: ${e.message}", e)
                                }
                            }
                        )
                    }
            }
        }

        // Tombol tambah perizinan (Floating Action Button)
        FloatingActionButton(
            onClick = { showBottomSheet.value = true },
            containerColor = appColors.primaryButtonColors,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_perizinan),
                    contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Tambah Perizinan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }

        // Bottom Sheet untuk form pengajuan perizinan
        if (showBottomSheet.value) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet.value = false },
                sheetState = bottomSheetState,
                containerColor = appColors.primaryBackground
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Masukkan alasan perizinan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.primaryText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = reasonText.value,
                        onValueChange = { reasonText.value = it },
                        label = { Text("Alasan Perizinan") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = appColors.secondaryBackground,
                            focusedTextColor = appColors.primaryText,
                            unfocusedTextColor = Color.DarkGray,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            absenceViewModel.postAbsence(
                                userEmail = currentEmail ?: "anonymous",
                                reason = reasonText.value,
                                onSuccess = {
                                    reasonText.value = ""
                                    showBottomSheet.value = false
                                },
                                onFailure = { error ->
                                    Log.e("PERIZINAN", "Gagal simpan izin: $error")
                                }
                            )
                            absenceViewModel.getAllAbsences()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryButtonColors),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kirim", color = Color.White)
                    }
                }
            }
        }
    }
}
