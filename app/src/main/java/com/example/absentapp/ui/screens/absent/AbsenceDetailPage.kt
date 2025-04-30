package com.example.absentapp.ui.screens.absent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.absentapp.R
import com.example.absentapp.data.model.Absence
import com.example.absentapp.data.model.Comment
import com.example.absentapp.ui.components.CustomTextField
import com.example.absentapp.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AbsenceDetailPage(
    absence: Absence,
    absenceViewModel: AbsenceViewModel,
    onCommentSubmit: (String) -> Unit,
    navController: NavHostController
) {
    val appColors = LocalAppColors.current
    var commentText by remember { mutableStateOf("") }

    val commentsMap by absenceViewModel.commentsMap.collectAsState()
    val comments = commentsMap[absence.id] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(appColors.primaryBackground)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .semantics(mergeDescendants = true) {
                isTraversalGroup = true
                traversalIndex = 1f
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "",
                tint = appColors.primaryText,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Detail Perizinan",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = appColors.primaryText,
                modifier = Modifier.semantics { heading() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== Post Content =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.primaryBackground, shape = RoundedCornerShape(12.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_profile),
                    contentDescription = "",
                    tint = appColors.secondaryText,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = absence.userEmail.substringBefore("@"),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = appColors.primaryText
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(absence.reason, fontSize = 14.sp, color = appColors.secondaryText)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID")).format(Date(absence.timestamp)),
                fontSize = 12.sp,
                color = appColors.secondaryText
            )
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // ===== Comment Input =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.primaryBackground, shape = RoundedCornerShape(12.dp))
        ) {
            CustomTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = "Tulis tanggapan Anda...",
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onCommentSubmit(commentText)
                    commentText = ""
                },
                enabled = commentText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryButtonColors)
            ) {
                Text("Kirim", color = Color.White)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // ===== Comment Section =====
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Komentar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = appColors.primaryText)
            Spacer(modifier = Modifier.height(8.dp))

            if (comments.isEmpty()) {
                Text("Belum ada komentar.", fontSize = 14.sp, color = appColors.secondaryText)
            } else {
                comments.forEach { comment ->
                    Row(modifier = Modifier.padding(4.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_profile),
                            contentDescription = "",
                            tint = appColors.secondaryText,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(appColors.secondaryBackground)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = comment.commenterEmail.substringBefore("@"),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = appColors.primaryText
                            )
                            Text(comment.commentText, fontSize = 14.sp, color = appColors.secondaryText)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

