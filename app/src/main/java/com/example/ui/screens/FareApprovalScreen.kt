package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.network.TripSyncManager
import kotlinx.coroutines.launch
import com.example.engine.RideMeterManager
import com.example.model.MeterMode
import com.example.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FareApprovalScreen(
    tripId: String,
    meterManager: RideMeterManager,
    onAcceptAndStart: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncManager = remember { TripSyncManager.getInstance(context) }
    val liveState by meterManager.liveState.collectAsState()
    val syncDebugStatus by syncManager.syncDebugStatus.collectAsState()
    val tariff = liveState.currentTariff
    val isExtraRide = liveState.mode == MeterMode.EXTRA_RIDE

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "معاينة واعتماد التسعيرة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("fare_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = AppTheme.colors.textPrimary
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Driver & Vehicle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(AppTheme.colors.primary, AppTheme.colors.appleGreen))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = AppTheme.colors.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    liveState.driverName,
                                    color = AppTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    liveState.driverCar,
                                    color = AppTheme.colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Trip ID Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.primaryContainer)
                                .border(1.dp, AppTheme.colors.primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                tripId.ifBlank { liveState.currentTripId },
                                color = AppTheme.colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppTheme.colors.bg)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("لوحة السيارة:", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(liveState.driverPlate, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("نوع المشوار:", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(liveState.mode.titleAr, color = AppTheme.colors.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tariff Breakdown Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = AppTheme.colors.primary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "تفاصيل التسعيرة المعتمدة رسمياً",
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tariff Metric Breakdown Rows
            TariffMetricRow(
                icon = Icons.Default.Speed,
                title = "فتح العداد (الرسوم الأساسية)",
                subtitle = "يبدأ الاحتساب فور الموافقة",
                value = "${String.format(java.util.Locale.US, "%.2f", if (isExtraRide) tariff.extraRideBaseFare else tariff.waitingBaseFare)} ${tariff.currency}",
                accentColor = AppTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            TariffMetricRow(
                icon = Icons.Default.Route,
                title = "سعر المسافة (للكيلومتر)",
                subtitle = "يُحسب عبر الـ GPS المباشر بالأمتار",
                value = "${String.format(java.util.Locale.US, "%.2f", if (isExtraRide) tariff.extraRidePerKm else tariff.waitingPerKm)} ${tariff.currency} / كم",
                accentColor = AppTheme.colors.appleGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            TariffMetricRow(
                icon = Icons.Default.HourglassTop,
                title = if (isExtraRide) "سعر دقيقة الرحلة" else "سعر دقيقة الانتظار",
                subtitle = "عداد زمني مستمر بالثواني والدقائق",
                value = "${String.format(java.util.Locale.US, "%.2f", if (isExtraRide) tariff.extraRidePerMin else tariff.waitingPerMin)} ${tariff.currency} / دقيقة",
                accentColor = AppTheme.colors.amber
            )

            Spacer(modifier = Modifier.height(10.dp))

            TariffMetricRow(
                icon = Icons.Default.Shield,
                title = "ضريبة القيمة المضافة (VAT)",
                subtitle = "شاملة للضريبة المطبقة",
                value = "${tariff.taxPercentage.toInt()}%",
                accentColor = AppTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Notice badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.colors.primaryContainer.copy(alpha = 0.5f))
                    .border(1.dp, AppTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "عند ضغطك على موافق يبدأ العداد بالعد مباشرة لدى السائق وتظهر لك التكلفة في الوقت الفعلي.",
                    color = AppTheme.colors.primary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Accept Button: "موافق على التسعيرة"
            Button(
                onClick = {
                    coroutineScope.launch {
                        syncManager.sendPassengerAcceptance(tripId, syncManager.lastKnownDriverIp)
                    }
                    val success = meterManager.passengerAcceptsFare(tripId)
                    if (success) {
                        Toast.makeText(context, "تمت الموافقة على التسعيرة وبدأ العداد!", Toast.LENGTH_SHORT).show()
                        onAcceptAndStart()
                    } else {
                        Toast.makeText(context, "تم بدء العداد للرحلة", Toast.LENGTH_SHORT).show()
                        onAcceptAndStart()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0x662563EB))
                    .testTag("accept_fare_button"),
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
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "موافق على التسعيرة وبدء العداد",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (syncDebugStatus.isNotBlank()) {
                Text(
                    syncDebugStatus,
                    color = AppTheme.colors.textMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Cancel / Back Button
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("cancel_fare_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = AppTheme.colors.surface),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(AppTheme.colors.border, AppTheme.colors.borderSubtle))
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = AppTheme.colors.rose)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إلغاء والعودة", color = AppTheme.colors.textSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun TariffMetricRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(AppTheme.colors.border, AppTheme.colors.borderSubtle))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(subtitle, color = AppTheme.colors.textSecondary, fontSize = 11.sp)
                }
            }
            Text(
                value,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
