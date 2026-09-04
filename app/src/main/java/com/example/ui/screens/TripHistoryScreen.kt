package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Route
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TripRepository
import com.example.model.MeterMode
import com.example.model.TripRecord
import com.example.ui.components.TripDisputeSheet
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    repository: TripRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val trips by repository.allTrips.collectAsState(initial = emptyList())
    var selectedDisputeTrip by remember { mutableStateOf<TripRecord?>(null) }
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = AppTheme.colors.appleGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "سجل الرحلات والفواتير",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("history_back_button")
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
                        modifier = Modifier.testTag("history_theme_toggle")
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
        if (trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.appleGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = AppTheme.colors.appleGreen, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "لا توجد رحلات مسجلة حتى الآن",
                        color = AppTheme.colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "عند اكتمال أي مشوار بالعداد، ستظهر البوليصة والبيانات وتدقيق المسار هنا",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trips, key = { it.tripId }) { trip ->
                    TripHistoryItem(
                        trip = trip,
                        onDisputeClick = { selectedDisputeTrip = trip }
                    )
                }
            }
        }
    }

    // Trip Dispute Sheet
    selectedDisputeTrip?.let { trip ->
        TripDisputeSheet(
            trip = trip,
            onDismiss = { selectedDisputeTrip = null },
            onSubmitDispute = { reason, notes, recKm, idealKm, excessKm, excessPct, origFare, corrFare, refund, mapUrl ->
                coroutineScope.launch {
                    repository.submitDispute(
                        trip = trip,
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
private fun TripHistoryItem(
    trip: TripRecord,
    onDisputeClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val minutes = trip.durationSeconds / 60
    val seconds = trip.durationSeconds % 60
    val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d دقيقة", minutes, seconds)
    val distKm = trip.distanceMeters / 1000.0
    val distFormatted = String.format(java.util.Locale.US, "%.2f كم (%d م)", distKm, trip.distanceMeters.toInt())

    val isExtraRide = trip.mode == MeterMode.EXTRA_RIDE
    val accentColor = if (isExtraRide) AppTheme.colors.appleGreen else AppTheme.colors.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    accentColor.copy(alpha = 0.5f),
                    AppTheme.colors.borderSubtle
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isExtraRide) Icons.Default.DirectionsCar else Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(trip.tripId, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (trip.isDisputed) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AppTheme.colors.appleGreenContainer)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("مدققة ومعوضة", color = AppTheme.colors.appleGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(trip.mode.titleAr, color = AppTheme.colors.textSecondary, fontSize = 11.sp)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${String.format(java.util.Locale.US, "%.2f", trip.totalFare)} ${trip.currency}",
                        color = AppTheme.colors.appleGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(trip.createdAtFormatted, color = AppTheme.colors.textMuted, fontSize = 10.sp)
                }
            }

            // Quick Metrics Row
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppTheme.colors.bg)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = AppTheme.colors.amber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(timeFormatted, color = AppTheme.colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Route, contentDescription = null, tint = AppTheme.colors.appleGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(distFormatted, color = AppTheme.colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expanded Breakdown & Dispute Action
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = AppTheme.colors.border)
                    Spacer(modifier = Modifier.height(8.dp))
                    HistoryRow("السائق:", "${trip.driverName} (${trip.driverPlate})")
                    HistoryRow("فتح العداد:", "${String.format(java.util.Locale.US, "%.2f", trip.baseFare)} ${trip.currency}")
                    HistoryRow("تكلفة المسافة:", "${String.format(java.util.Locale.US, "%.2f", trip.distanceCost)} ${trip.currency}")
                    HistoryRow("تكلفة الوقت:", "${String.format(java.util.Locale.US, "%.2f", trip.timeCost)} ${trip.currency}")
                    HistoryRow("الضريبة:", "${String.format(java.util.Locale.US, "%.2f", trip.taxAmount)} ${trip.currency}")

                    if (trip.platformFeeAmount > 0) {
                        HistoryRow("رسوم المنصة المستقطعة:", "${String.format(java.util.Locale.US, "%.2f", trip.platformFeeAmount)} ${trip.currency}")
                        HistoryRow("صافي مستحقات الكابتن:", "${String.format(java.util.Locale.US, "%.2f", trip.driverNetPayout)} ${trip.currency}")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!trip.isDisputed) {
                        OutlinedButton(
                            onClick = onDisputeClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.rose)
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = AppTheme.colors.rose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تدقيق المسار ومطابقة Google Maps (اعتراض)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.appleGreenContainer.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppTheme.colors.appleGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "تم فحص المسار وتعويضك بمبلغ ${String.format(java.util.Locale.US, "%.2f", trip.suggestedRefundAmount)} ريال في محفظتك.",
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.appleGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AppTheme.colors.textSecondary, fontSize = 11.sp)
        Text(value, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}
