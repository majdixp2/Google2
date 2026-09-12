package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.network.TripSyncManager
import com.example.engine.LiveMeterState
import com.example.engine.RideMeterManager
import com.example.model.MeterMode
import com.example.model.TripStatus
import com.example.ui.components.QrCodeView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DriverHomeScreen(
    meterManager: RideMeterManager,
    onNavigateBack: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSupport: (() -> Unit)? = null,
    onNavigateToAccount: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val syncManager = remember { TripSyncManager.getInstance(context) }

    // Start background sync server so passenger phone can connect directly or via cloud
    DisposableEffect(Unit) {
        syncManager.startDriverServer(meterManager)
        onDispose {
            // Keep server running or stop
        }
    }

    // Real GPS distance tracking requires this runtime permission — request it as soon
    // as the driver opens their meter screen, and (re)start location updates once granted.
    val locationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            meterManager.startLocationUpdates()
        }
    }

    val liveState by meterManager.liveState.collectAsState()
    val isDark by ThemeManager.isDarkMode.collectAsState()
    var showQrDialog by remember { mutableStateOf(false) }

    // Auto-close the QR/waiting dialog the instant the passenger accepts and the meter
    // actually starts counting — the driver shouldn't have to manually close it.
    LaunchedEffect(liveState.isCounting) {
        if (liveState.isCounting) {
            showQrDialog = false
        }
    }
    val localIp = remember { syncManager.getLocalIpAddress() }
    val syncDebugStatus by syncManager.syncDebugStatus.collectAsState()

    val isStopped = liveState.status == TripStatus.STOPPED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "لوحة التحكم - السائق",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .testTag("driver_back_button")
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
                        onClick = { ThemeManager.toggleDarkMode() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .testTag("driver_dark_mode_button")
                    ) {
                        Icon(
                            if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تبديل المظهر الداكن",
                            tint = if (isDark) AppTheme.colors.appleGreen else AppTheme.colors.textSecondary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .testTag("driver_history_button")
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "سجل الرحلات",
                            tint = AppTheme.colors.textSecondary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .testTag("driver_admin_button")
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "أسعار الأدمن",
                            tint = AppTheme.colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surface,
                    titleContentColor = AppTheme.colors.textPrimary
                ),
                modifier = Modifier.border(width = 1.dp, color = AppTheme.colors.border)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AppTheme.colors.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.border(width = 1.dp, color = AppTheme.colors.border)
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Stay on Home */ },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppTheme.colors.primary,
                        selectedTextColor = AppTheme.colors.primary,
                        indicatorColor = AppTheme.colors.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Default.History, contentDescription = "السجل") },
                    label = { Text("السجل", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppTheme.colors.textMuted,
                        unselectedTextColor = AppTheme.colors.textMuted
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToAccount?.invoke() ?: onNavigateToAdmin() },
                    icon = { Icon(Icons.Default.Person, contentDescription = "الحساب") },
                    label = { Text("الحساب", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppTheme.colors.textMuted,
                        unselectedTextColor = AppTheme.colors.textMuted
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToSupport?.invoke() ?: onNavigateToAdmin() },
                    icon = { Icon(Icons.Default.HeadsetMic, contentDescription = "الدعم") },
                    label = { Text("الدعم", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppTheme.colors.textMuted,
                        unselectedTextColor = AppTheme.colors.textMuted
                    )
                )
            }
        },
        containerColor = AppTheme.colors.bg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // IF STOPPED -> SHOW DRIVER FINAL RESULT SUMMARY AND RESUME BUTTON
            if (isStopped) {
                DriverStoppedResultCard(
                    liveState = liveState,
                    onResumeMistake = {
                        meterManager.resumeTripIfStoppedByMistake()
                        Toast.makeText(context, "تم استئناف الرحلة بنجاح واستمرار العداد", Toast.LENGTH_SHORT).show()
                    },
                    onFinishAndSave = {
                        meterManager.finishAndSaveTrip()
                        Toast.makeText(context, "تم حفظ الرحلة بنجاح في السجل", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // 1. TOP MODE SELECTION BUTTONS (انتظار مدفوع / مشوار إضافي)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DriverModeButton(
                        modifier = Modifier.weight(1f),
                        title = "انتظار مدفوع",
                        subtitle = if (liveState.mode == MeterMode.PAID_WAITING) "الحالة الحالية" else "تبديل النمط",
                        isSelected = liveState.mode == MeterMode.PAID_WAITING,
                        tag = "driver_card_waiting",
                        onClick = {
                            if (!liveState.isCounting) {
                                meterManager.selectMode(MeterMode.PAID_WAITING)
                            }
                        }
                    )

                    DriverModeButton(
                        modifier = Modifier.weight(1f),
                        title = "مشوار إضافي",
                        subtitle = if (liveState.mode == MeterMode.EXTRA_RIDE) "الحالة الحالية" else "طلب جديد",
                        isSelected = liveState.mode == MeterMode.EXTRA_RIDE,
                        tag = "driver_card_extra_ride",
                        onClick = {
                            if (!liveState.isCounting) {
                                meterManager.selectMode(MeterMode.EXTRA_RIDE)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. THE 3 METRIC BOXES ROW (الوقت - المسافة - فتح العداد)
                val isModeActive = true
                val minutes = liveState.durationSeconds / 60
                val seconds = liveState.durationSeconds % 60
                val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d:%02d", liveState.durationSeconds / 3600, minutes % 60, seconds)
                val distKm = liveState.distanceMeters / 1000.0
                val distFormatted = String.format(java.util.Locale.US, "%.1f كم", distKm)
                val baseFare = if (liveState.mode == MeterMode.EXTRA_RIDE) liveState.currentTariff.extraRideBaseFare else liveState.currentTariff.waitingBaseFare

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.HourglassTop,
                        iconTint = AppTheme.colors.primary,
                        iconBg = AppTheme.colors.blueTint,
                        label = "الوقت",
                        value = timeFormatted,
                        tag = "metric_time_box_${liveState.mode.name}"
                    )

                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.GpsFixed,
                        iconTint = AppTheme.colors.appleGreen,
                        iconBg = AppTheme.colors.appleGreenContainer,
                        label = "المسافة",
                        value = distFormatted,
                        tag = "metric_dist_box_${liveState.mode.name}"
                    )

                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        iconTint = AppTheme.colors.amber,
                        iconBg = AppTheme.colors.amberContainer,
                        label = "فتح العداد",
                        value = String.format(java.util.Locale.US, "%.2f", baseFare),
                        tag = "metric_base_box_${liveState.mode.name}"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. CENTRAL LIVE TOTAL FARE DISPLAY CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = Color(0x332563EB)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF2563EB),
                                Color(0xFF38BDF8).copy(alpha = 0.6f),
                                Color(0xFF1E3A8A)
                            )
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
                                .padding(vertical = 30.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "المجموع المباشر للأجرة",
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
                                    text = String.format(java.util.Locale.US, "%.2f", liveState.totalFare),
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontSize = 62.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = (-1.5).sp,
                                    modifier = Modifier.testTag("driver_live_total_text")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = liveState.currentTariff.currency,
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1D4ED8),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // GPS Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (liveState.isCounting) {
                                            if (isDark) Color(0xFF0C274E) else Color(0xFFDBEAFE)
                                        } else {
                                            AppTheme.colors.surfaceVariant
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (liveState.isCounting) Color(0xFF2563EB)
                                        else AppTheme.colors.border,
                                        RoundedCornerShape(24.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (liveState.isCounting) {
                                                    if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
                                                } else {
                                                    AppTheme.colors.textMuted
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (liveState.isCounting) "يتم الحساب الآن عبر GPS بالأمتار" else "العداد جاهز للتشغيل",
                                        color = if (liveState.isCounting) {
                                            if (isDark) Color(0xFFE0F2FE) else Color(0xFF1E3A8A)
                                        } else {
                                            AppTheme.colors.textSecondary
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. ACTION BUTTONS (Start/Pause, Stop, Share Barcode)
                // Start / Pause
                if (!liveState.isCounting) {
                    // Start Button (Electric Blue Gradient)
                    Button(
                        onClick = {
                            meterManager.startCounting()
                            Toast.makeText(context, "بدأ العداد في الحساب", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0x662563EB))
                            .testTag("driver_start_button"),
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
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("بدء تشغيل العداد", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                } else {
                    // Pause Button (Warm Amber Gradient)
                    Button(
                        onClick = {
                            meterManager.pauseCounting()
                            Toast.makeText(context, "تم إيقاف العداد مؤقتاً", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0x66F59E0B))
                            .testTag("driver_pause_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFD97706), Color(0xFFF59E0B))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFF1E1402), modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إيقاف مؤقت للعداد", color = Color(0xFF1E1402), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stop Final Button (Rose / Crimson Gradient)
                Button(
                    onClick = {
                        meterManager.stopTrip()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x44EF4444))
                        .testTag("driver_stop_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFB91C1C), Color(0xFFDC2626), Color(0xFFEF4444))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إيقاف العداد النهائي", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Share Barcode Button (Sleek Glassmorphic Button with Electric Blue border)
                OutlinedButton(
                    onClick = {
                        meterManager.shareBarcode(localIp)
                        showQrDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("share_barcode_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = AppTheme.colors.surfaceVariant.copy(alpha = 0.5f)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF38BDF8))),
                        width = 1.5.dp
                    )
                ) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "مشاركة الباركود للراكب (${liveState.currentTripId})",
                        color = if (isDark) Color.White else Color(0xFF1E3A8A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // QR CODE DISPLAY DIALOG
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            containerColor = AppTheme.colors.surface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "باركود الرحلة للراكب",
                        color = AppTheme.colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "رقم الرحلة: ${liveState.currentTripId}",
                        color = AppTheme.colors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "اطلب من الراكب مسح الباركود لعرض التسعيرة والبدء المباشر",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Canvas
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .border(2.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    ) {
                        QrCodeView(
                            data = liveState.qrPayload.ifBlank { liveState.currentTripId },
                            backgroundColor = Color.White,
                            codeColor = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "نوع العداد: ${liveState.mode.titleAr}",
                        color = AppTheme.colors.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "فتح العداد: ${String.format(java.util.Locale.US, "%.2f", liveState.baseFare)} ريال",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Connection Sync Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = AppTheme.colors.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "مزامنة سحابية + شبكة محلية ($localIp)",
                            color = AppTheme.colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (syncDebugStatus.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            syncDebugStatus,
                            color = AppTheme.colors.textMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun DriverModeButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1D4ED8) else AppTheme.colors.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    if (isSelected) Color(0xFF38BDF8) else AppTheme.colors.border,
                    if (isSelected) Color(0xFF2563EB) else AppTheme.colors.borderSubtle
                )
            ),
            width = if (isSelected) 2.dp else 1.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1D4ED8), Color(0xFF2563EB))
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(AppTheme.colors.surface, AppTheme.colors.surface)
                        )
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    subtitle.uppercase(),
                    color = if (isSelected) Color(0xFFBAE6FD) else AppTheme.colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    title,
                    color = if (isSelected) Color.White else AppTheme.colors.textSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    tag: String
) {
    Card(
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(AppTheme.colors.border, AppTheme.colors.borderSubtle))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                label,
                color = AppTheme.colors.textMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                color = AppTheme.colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DriverStoppedResultCard(
    liveState: LiveMeterState,
    onResumeMistake: () -> Unit,
    onFinishAndSave: () -> Unit
) {
    val minutes = liveState.durationSeconds / 60
    val seconds = liveState.durationSeconds % 60
    val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d دقيقة", minutes, seconds)
    val distKm = liveState.distanceMeters / 1000.0
    val distFormatted = String.format(java.util.Locale.US, "%.2f كم (%d م)", distKm, liveState.distanceMeters.toInt())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_result_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(AppTheme.colors.border, AppTheme.colors.border))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.roseContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, tint = AppTheme.colors.rose, modifier = Modifier.size(30.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("تم إيقاف العداد ونتيجة الرحلة", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("رقم الرحلة: ${liveState.currentTripId}", color = AppTheme.colors.textSecondary, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Grand Total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppTheme.colors.bg)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجمالي الأجرة المستحقة", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${String.format(java.util.Locale.US, "%.2f", liveState.totalFare)} ${liveState.currentTariff.currency}",
                        color = AppTheme.colors.appleGreen,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calculation Breakdown Table
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("فتح العداد:", color = AppTheme.colors.textSecondary, fontSize = 13.sp)
                Text("${String.format(java.util.Locale.US, "%.2f", liveState.baseFare)} ريال", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("حساب المسافة ($distFormatted):", color = AppTheme.colors.textSecondary, fontSize = 13.sp)
                Text("${String.format(java.util.Locale.US, "%.2f", liveState.distanceCost)} ريال", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("حساب الوقت ($timeFormatted):", color = AppTheme.colors.textSecondary, fontSize = 13.sp)
                Text("${String.format(java.util.Locale.US, "%.2f", liveState.timeCost)} ريال", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("الضريبة (${liveState.currentTariff.taxPercentage.toInt()}%):", color = AppTheme.colors.textSecondary, fontSize = 13.sp)
                Text("${String.format(java.util.Locale.US, "%.2f", liveState.taxAmount)} ريال", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            HorizontalDivider(color = AppTheme.colors.border, modifier = Modifier.padding(vertical = 16.dp))

            // CRITICAL MANDATED BUTTON: "استمرار في حال ضغط ايقاف عن طريق الخطأ"
            Button(
                onClick = onResumeMistake,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66F59E0B))
                    .testTag("driver_resume_mistake_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFD97706), Color(0xFFF59E0B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Replay, contentDescription = null, tint = Color(0xFF1E1402), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "استمرار (في حال ضغط إيقاف بالخطأ)",
                            color = Color(0xFF1E1402),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save & Complete Button
            Button(
                onClick = onFinishAndSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x662563EB))
                    .testTag("driver_save_finish_button"),
                shape = RoundedCornerShape(16.dp),
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
                        Text("إنهاء وحفظ الرحلة وإعادة التعيين", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

