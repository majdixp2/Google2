package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    codeColor: Color = Color(0xFF0F172A)
) {
    val matrixSize = 25
    // Deterministic pseudo QR module matrix based on data hash
    val modules = BooleanArray(matrixSize * matrixSize) { index ->
        val row = index / matrixSize
        val col = index % matrixSize

        // Top-left finder pattern
        if (row < 7 && col < 7) {
            return@BooleanArray (row == 0 || row == 6 || col == 0 || col == 6 || (row in 2..4 && col in 2..4))
        }
        // Top-right finder pattern
        if (row < 7 && col >= matrixSize - 7) {
            val c = col - (matrixSize - 7)
            return@BooleanArray (row == 0 || row == 6 || c == 0 || c == 6 || (row in 2..4 && c in 2..4))
        }
        // Bottom-left finder pattern
        if (row >= matrixSize - 7 && col < 7) {
            val r = row - (matrixSize - 7)
            return@BooleanArray (r == 0 || r == 6 || col == 0 || col == 6 || (r in 2..4 && col in 2..4))
        }
        // Timing patterns
        if (row == 6 || col == 6) {
            return@BooleanArray (row + col) % 2 == 0
        }
        // Center alignment pattern
        if (row in 16..20 && col in 16..20) {
            val r = row - 16
            val c = col - 16
            return@BooleanArray (r == 0 || r == 4 || c == 0 || c == 4 || (r == 2 && c == 2))
        }

        // Data encoding module calculation
        val charVal = if (data.isNotEmpty()) data[(row * 7 + col * 13) % data.length].code else 42
        val hash = abs((data.hashCode() xor (row * 31 + col * 17) xor (charVal * 19)))
        (hash % 3 == 0 || hash % 7 == 0 || (row + col) % 3 == 0)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        ) {
            val moduleSize = size.width / matrixSize
            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (modules[r * matrixSize + c]) {
                        // Finder corners outer styling
                        val isFinder = (r < 7 && c < 7) || (r < 7 && c >= matrixSize - 7) || (r >= matrixSize - 7 && c < 7)
                        drawRoundRect(
                            color = if (isFinder) codeColor else codeColor.copy(alpha = 0.92f),
                            topLeft = Offset(c * moduleSize, r * moduleSize),
                            size = Size(moduleSize * 0.92f, moduleSize * 0.92f),
                            cornerRadius = CornerRadius(moduleSize * 0.2f, moduleSize * 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QrScannerViewfinder(
    modifier: Modifier = Modifier,
    onScanned: () -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "scan_laser")
    val laserY by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.92f))
            .border(2.dp, Color(0xFF22C55E).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerLen = 32.dp.toPx()
            val strokeW = 4.dp.toPx()
            val appleGreen = Color(0xFF22C55E)

            // Top-Left corner bracket
            drawLine(appleGreen, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW)
            drawLine(appleGreen, Offset(0f, 0f), Offset(0f, cornerLen), strokeW)

            // Top-Right corner bracket
            drawLine(appleGreen, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), strokeW)
            drawLine(appleGreen, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeW)

            // Bottom-Left corner bracket
            drawLine(appleGreen, Offset(0f, size.height), Offset(cornerLen, size.height), strokeW)
            drawLine(appleGreen, Offset(0f, size.height), Offset(0f, size.height - cornerLen), strokeW)

            // Bottom-Right corner bracket
            drawLine(appleGreen, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), strokeW)
            drawLine(appleGreen, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), strokeW)

            // Animated Scanning Laser Line
            val currentLaserY = size.height * laserY
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xFF4ADE80),
                        Color(0xFF22C55E),
                        Color(0xFF4ADE80),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(8.dp.toPx(), currentLaserY - 2.dp.toPx()),
                size = Size(size.width - 16.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}
