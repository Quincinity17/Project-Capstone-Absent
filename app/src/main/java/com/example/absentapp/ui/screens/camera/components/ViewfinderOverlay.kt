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