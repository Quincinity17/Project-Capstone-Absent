package com.example.absentapp.ui.screens.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ViewfinderOverlay menampilkan area "lubang transparan" di tengah layar
 * yang dapat digunakan sebagai panduan untuk menempatkan wajah atau objek.
 *
 * @param widthPercent Persentase lebar lubang terhadap layar (0.0 - 1.0)
 * @param heightPercent Persentase tinggi lubang terhadap layar (0.0 - 1.0)
 * @param verticalOffsetPercent Posisi lubang secara vertikal (positif ke bawah)
 * @param cornerRadius Radius sudut untuk lubang viewfinder
 */
@Composable
fun ViewfinderOverlay(
    modifier: Modifier = Modifier,
    widthPercent: Float,
    heightPercent: Float,
    verticalOffsetPercent: Float = 0f,
    cornerRadius: Dp = 24.dp
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Konversi ukuran layout ke pixel
        val canvasWidth = with(LocalDensity.current) { maxWidth.toPx() }
        val canvasHeight = with(LocalDensity.current) { maxHeight.toPx() }
        val cornerRadiusPx = with(LocalDensity.current) { cornerRadius.toPx() }

        // Hitung ukuran dan posisi lubang transparan
        val holeWidthPx = canvasWidth * widthPercent
        val holeHeightPx = canvasHeight * heightPercent
        val verticalOffsetPx = canvasHeight * verticalOffsetPercent

        val left = (canvasWidth - holeWidthPx) / 2f
        val top = (canvasHeight - holeHeightPx) / 2f + verticalOffsetPx

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Gambar overlay hitam semi-transparan
            drawRect(color = Color.Black.copy(alpha = 0.5f))

            // Gambar lubang transparan di tengah (menggunakan BlendMode.Clear)
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(holeWidthPx, holeHeightPx),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                blendMode = BlendMode.Clear // membersihkan area agar terlihat
            )
        }
    }
}
