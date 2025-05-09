package com.example.absentapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.absentapp.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationBottomSheet(
    title: String,
    description: String,
    iconResId: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onFirstButton: () -> Unit,
    onSecondButton: () -> Unit,
    firstText: String,
    secondText: String
) {
    val appColors = LocalAppColors.current

    // BottomSheet kustom untuk menampilkan konfirmasi kepada pengguna
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = appColors.primaryBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gambar ilustrasi
            Image(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(180.dp)
            )

            // Judul dan deskripsi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "$title. $description"
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dua tombol aksi
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tombol pertama (biasanya aksi utama)
                Button(
                    onClick = onFirstButton,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryButtonColors)
                ) {
                    Text(firstText, color = Color.White)
                }

                // Tombol kedua
                Button(
                    onClick = onSecondButton,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryBackground),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(secondText, color = appColors.primaryButtonColors)
                }
            }
        }
    }
}
