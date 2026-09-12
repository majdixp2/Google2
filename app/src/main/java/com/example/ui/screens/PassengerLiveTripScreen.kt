package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.DisposableEffect
import com.example.network.TripSyncManager
import com.example.data.TripRepository
import com.example.engine.LiveMeterState
import com.example.engine.RideMeterManager
import com.example.model.MeterMode
import com.example.model.TripRecord
import com.example.model.TripStatus
import com.example.ui.components.TripDisputeSheet
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerLiveTripScreen(
    meterManager: RideMeterManager,
    repository: TripRepository? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncManager = remember { TripSyncManager.getInstance(context) }
    val liveState by meterManager.liveState.collectAsState()
    val syncDebugStatus by syncManager.syncDebugStatus.collectAsState()
    val isStopped = liveState.status == TripStatus.STOPPED || liveState.status == TripStatus.COMPLETED
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    // Ensure passenger sync is running for this trip if active
    DisposableEffect(liveState.currentTripId) {
        if (liveState.currentTripId.isNotBlank()) {
            syncManager.startPassengerSync(
                meterManager = meterManager,
                tripId = liveState.currentTripId,
                driverIp = syncManager.lastKnownDriverIp ?: ""
            )
        }
        onDispose { }
    }

    var rating by remember { mutableIntStateOf(5) }
    var showDisputeSheet by remember { mutableStateOf(false) }

    // Pulse animation for live meter indicator
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = AppTheme.colors.appleGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isStopped) "بوليصة الرحلة والفاتورة" else "شاشة عداد الرحلة الحية",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("passenger_live_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { ThemeManager.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_live")
                    ) {
                        Icon(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تبديل المظهر",
                            tint = AppTheme.colors.appleGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.surface),
                modifier = Modifier.border(1.dp, AppTheme.colors.border)
            )
        },
        containerColor = AppTheme.colors.bg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (!isStopped) {
                // LIVE RUNNING METER VIEW
                LiveRunningHeader(liveState = liveState, pulseScale = pulseScale, syncDebugStatus = syncDebugStatus)

                // Big Total Fare Counter Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = Color(0x332563EB))
                        .testTag("passenger_live_total_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF38BDF8), Color(0xFF1E3A8A))
                        ),
                        width = 1.5.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        AppTheme.colors.surfaceVariant.copy(alpha = 0.7f),
                                        AppTheme.colors.surface
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "الأجرة الإجمالية المباشرة",
                                color = AppTheme.colors.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    String.format(java.util.Locale.US, "%.2f", liveState.totalFare),
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = (-1.5).sp,
                                    modifier = Modifier.testTag("passenger_live_total_value")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    liveState.currentTariff.currency,
                                    color = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF1D4ED8),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Base Fare breakdown label
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isDarkMode) Color(0xFF0E2A68) else Color(0xFFDBEAFE))
                                    .border(1.dp, if (isDarkMode) Color(0xFF2563EB) else Color(0xFF93C5FD), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "شامل فتح العداد (${String.format(java.util.Locale.US, "%.2f", liveState.baseFare)} ريال) + ضريبة VAT",
                                    color = if (isDarkMode) Color(0xFFBAE6FD) else Color(0xFF1E3A8A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Metric Cards: Time & Distance
                val minutes = liveState.durationSeconds / 60
                val seconds = liveState.durationSeconds % 60
                val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
                val distKm = liveState.distanceMeters / 1000.0
                val distFormatted = String.format(java.util.Locale.US, "%.2f كم", distKm)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricBox(
                        title = "الوقت المنقضي",
                        value = timeFormatted,
                        cost = "${String.format(java.util.Locale.US, "%.2f", liveState.timeCost)} ريال",
                        icon = Icons.Default.HourglassBottom,
                        accentColor = AppTheme.colors.amber,
                        modifier = Modifier.weight(1f)
                    )

                    MetricBox(
                        title = if (liveState.mode == MeterMode.EXTRA_RIDE) "المسافة المقطوعة" else "حالة التوقف",
                        value = if (liveState.mode == MeterMode.EXTRA_RIDE) distFormatted else "ثابت",
                        cost = if (liveState.mode == MeterMode.EXTRA_RIDE) "${String.format(java.util.Locale.US, "%.2f", liveState.distanceCost)} ريال" else "0.00 ريال",
                        icon = Icons.Default.Route,
                        accentColor = AppTheme.colors.appleGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Vehicle & Driver Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(AppTheme.colors.border, AppTheme.colors.borderSubtle))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = AppTheme.colors.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(liveState.driverName, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${liveState.driverCar} • لوحة: ${liveState.driverPlate}", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.appleGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = AppTheme.colors.appleGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GPS نشط", color = AppTheme.colors.appleGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

            } else {
                // FINAL POLICY / RECEIPT CARD WITH GOOGLE MAPS DISPUTE BUTTON
                FinalReceiptCard(
                    liveState = liveState,
                    rating = rating,
                    onRatingChange = { rating = it },
                    onDisputeClick = {
                        showDisputeSheet = true
                    },
                    onFinish = {
                        meterManager.finishAndSaveTrip()
                        onNavigateBack()
                    }
                )
            }
        }
    }

    // Google Maps Route Dispute & Verification Bottom Sheet
    if (showDisputeSheet) {
        val currentTrip = TripRecord(
            tripId = liveState.currentTripId,
            mode = liveState.mode,
            status = liveState.status,
            driverId = liveState.driverId,
            driverName = liveState.driverName,
            driverPlate = liveState.driverPlate,
            driverCar = liveState.driverCar,
            passengerId = liveState.passengerId,
            passengerName = liveState.passengerName,
            startLocationName = liveState.startLocationName,
            endLocationName = liveState.endLocationName,
            startLat = liveState.startLat,
            startLng = liveState.startLng,
            endLat = liveState.endLat,
            endLng = liveState.endLng,
            durationSeconds = liveState.durationSeconds,
            distanceMeters = liveState.distanceMeters,
            idealDistanceMeters = liveState.idealDistanceMeters,
            idealDurationSeconds = liveState.idealDurationSeconds,
            baseFare = liveState.baseFare,
            distanceCost = liveState.distanceCost,
            timeCost = liveState.timeCost,
            taxAmount = liveState.taxAmount,
            totalFare = liveState.totalFare,
            currency = liveState.currentTariff.currency
        )

        TripDisputeSheet(
            trip = currentTrip,
            onDismiss = { showDisputeSheet = false },
            onSubmitDispute = { reason, notes, recKm, idealKm, excessKm, excessPct, origFare, corrFare, refund, mapUrl ->
                coroutineScope.launch {
                    repository?.submitDispute(
                        trip = currentTrip,
                        reasonCategory = reason,
                        passengerNotes = notes,
                        recordedDistanceKm = recKm,
                        idealDistanceKm = idealKm,
                        excessDistanceKm = excessKm,
                        excessPercentage = excessPct,
                        originalFare = origFare,
                        correctedFare = corrFare,
                        refundAmount = refund,
                        googleMapsUrl = mapUrl
                    )
                }
            }
        )
    }
}

@Composable
private fun LiveRunningHeader(liveState: LiveMeterState, pulseScale: Float, syncDebugStatus: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(if (liveState.isCounting) AppTheme.colors.appleGreen else AppTheme.colors.amber)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (liveState.isCounting) "العداد يحسب الآن بدقة..." else "العداد متوقف مؤقتاً",
                color = if (liveState.isCounting) AppTheme.colors.appleGreen else AppTheme.colors.amber,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppTheme.colors.appleGreenContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = AppTheme.colors.appleGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("متزامن حي", color = AppTheme.colors.appleGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppTheme.colors.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    liveState.mode.titleAr,
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        if (syncDebugStatus.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                syncDebugStatus,
                color = AppTheme.colors.textMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    textDirection = androidx.compose.ui.text.style.TextDirection.Ltr
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    cost: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(AppTheme.colors.border, AppTheme.colors.borderSubtle))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(title, color = AppTheme.colors.textSecondary, fontSize = 11.sp)
            Text(value, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text("+$cost", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FinalReceiptCard(
    liveState: LiveMeterState,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    onDisputeClick: () -> Unit,
    onFinish: () -> Unit
) {
    val minutes = liveState.durationSeconds / 60
    val seconds = liveState.durationSeconds % 60
    val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d دقيقة", minutes, seconds)
    val distKm = liveState.distanceMeters / 1000.0
    val distFormatted = String.format(java.util.Locale.US, "%.2f كم (%d م)", distKm, liveState.distanceMeters.toInt())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("passenger_final_receipt_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(AppTheme.colors.primary, AppTheme.colors.appleGreen))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.appleGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppTheme.colors.appleGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "بوليصة الرحلة والفاتورة النهائية",
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Text(
                "رقم البوليصة: ${liveState.currentTripId}",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total Amount Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.bg)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("المبلغ المطلوب للدفع", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${String.format(java.util.Locale.US, "%.2f", liveState.totalFare)} ${liveState.currentTariff.currency}",
                        color = AppTheme.colors.appleGreen,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Breakdown
            ReceiptRow(label = "فتح العداد", value = "${String.format(java.util.Locale.US, "%.2f", liveState.baseFare)} ريال")
            ReceiptRow(label = "تكلفة المسافة ($distFormatted)", value = "${String.format(java.util.Locale.US, "%.2f", liveState.distanceCost)} ريال")
            ReceiptRow(label = "تكلفة الوقت ($timeFormatted)", value = "${String.format(java.util.Locale.US, "%.2f", liveState.timeCost)} ريال")
            ReceiptRow(label = "ضريبة القيمة المضافة (${liveState.currentTariff.taxPercentage.toInt()}%)", value = "${String.format(java.util.Locale.US, "%.2f", liveState.taxAmount)} ريال")

            HorizontalDivider(color = AppTheme.colors.border, modifier = Modifier.padding(vertical = 12.dp))

            // OBJECTION & GOOGLE MAPS AUDIT BUTTON (زر اعتراض على الرحلة والمسار)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDisputeClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.roseContainer.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(AppTheme.colors.rose, AppTheme.colors.rose.copy(alpha = 0.5f)))
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.roseContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = AppTheme.colors.rose, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("هل تشك بزيادة السائق للمسار؟", color = AppTheme.colors.rose, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("تدقيق المسار ومقارنته مع Google Maps واسترداد الفارق", color = AppTheme.colors.textSecondary, fontSize = 10.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.rose)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("اعتراض", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Driver Rating
            Text("تقييم السائق", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                for (i in 1..5) {
                    IconButton(onClick = { onRatingChange(i) }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "نجمة $i",
                            tint = if (i <= rating) AppTheme.colors.amber else AppTheme.colors.border,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0x662563EB))
                    .testTag("passenger_finish_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1D4ED8), Color(0xFF2563EB), Color(0xFF0284C7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تأكيد الدفع والعودة للرئيسية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AppTheme.colors.textSecondary, fontSize = 12.sp)
        Text(value, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}
