package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.concurrent.Executors

/**
 * Renders a REAL, scannable QR code encoding [data] using ZXing.
 * Any standard QR reader (including [RealQrCameraScanner] below) can decode it.
 */
@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    codeColor: Color = Color(0xFF0F172A)
) {
    val bitmap = remember(data, codeColor) {
        val size = 512
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data.ifBlank { "ADDAD" }, BarcodeFormat.QR_CODE, size, size)
        val fgArgb = AndroidColor.rgb(
            (codeColor.red * 255).toInt(),
            (codeColor.green * 255).toInt(),
            (codeColor.blue * 255).toInt()
        )
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) fgArgb else AndroidColor.WHITE)
            }
        }
        bmp
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "رمز QR للرحلة",
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        )
    }
}

/**
 * Live camera preview that decodes QR / barcodes in real time using ML Kit.
 * Calls [onScanned] with the raw decoded string the first time a code is found,
 * then stops further analysis until the composable is re-created.
 */
@Composable
fun RealQrCameraScanner(
    modifier: Modifier = Modifier,
    onScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasScanned by remember { mutableStateOf(false) }
    val currentOnScanned by rememberUpdatedState(onScanned)

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
            .border(2.dp, Color(0xFF22C55E).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val executor = Executors.newSingleThreadExecutor()
                val scanner = BarcodeScanning.getClient()

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(executor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !hasScanned) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val value = barcodes.firstOrNull { it.rawValue != null }?.rawValue
                                    if (value != null && !hasScanned) {
                                        hasScanned = true
                                        currentOnScanned(value)
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    } catch (_: Exception) {
                        // Camera bind failed silently; the manual trip-code field remains available.
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Corner brackets + animated laser overlay (visual guide only)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerLen = 32.dp.toPx()
            val strokeW = 4.dp.toPx()
            val appleGreen = Color(0xFF22C55E)

            drawLine(appleGreen, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW)
            drawLine(appleGreen, Offset(0f, 0f), Offset(0f, cornerLen), strokeW)
            drawLine(appleGreen, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), strokeW)
            drawLine(appleGreen, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeW)
            drawLine(appleGreen, Offset(0f, size.height), Offset(cornerLen, size.height), strokeW)
            drawLine(appleGreen, Offset(0f, size.height), Offset(0f, size.height - cornerLen), strokeW)
            drawLine(appleGreen, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), strokeW)
            drawLine(appleGreen, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), strokeW)

            val currentLaserY = size.height * laserY
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color(0xFF4ADE80), Color(0xFF22C55E), Color(0xFF4ADE80), Color.Transparent)
                ),
                topLeft = Offset(8.dp.toPx(), currentLaserY - 2.dp.toPx()),
                size = Size(size.width - 16.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

/**
 * Decorative-only viewfinder kept for places that don't yet have camera permission
 * or want a placeholder (e.g. permission rationale screen).
 */
@Composable
fun QrScannerViewfinder(
    modifier: Modifier = Modifier,
    onScanned: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.92f))
            .border(2.dp, Color(0xFF22C55E).copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "بانتظار إذن الكاميرا...",
            color = Color(0xFF9AA7BD)
        )
    }
}
