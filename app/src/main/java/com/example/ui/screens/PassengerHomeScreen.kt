package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Wifi
import com.example.network.TripSyncManager
import com.example.engine.LiveMeterState
import com.example.engine.RideMeterManager
import com.example.model.TripStatus
import com.example.ui.components.QrScannerViewfinder
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerHomeScreen(
    meterManager: RideMeterManager,
    onNavigateBackToLogin: () -> Unit,
    onNavigateToFareApproval: (String) -> Unit,
    onNavigateToLiveTrip: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncManager = remember { TripSyncManager.getInstance(context) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val liveState by meterManager.liveState.collectAsState()
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    var showScannerDialog by remember { mutableStateOf(false) }
    var showEnterTripIdDialog by remember { mutableStateOf(false) }
    var inputTripId by remember { mutableStateOf("") }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var manualBarcodeText by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = AppTheme.colors.surface,
                drawerContentColor = AppTheme.colors.textPrimary,
                modifier = Modifier.width(310.dp)
            ) {
                // Passenger Drawer Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(AppTheme.colors.surfaceVariant, AppTheme.colors.surface)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.appleGreenContainer)
                                    .border(2.dp, AppTheme.colors.appleGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "الراكب",
                                    tint = AppTheme.colors.appleGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "محمد السعدي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = "passenger@example.com",
                                    fontSize = 12.sp,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Wallet Mini Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.bg)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = AppTheme.colors.appleGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("رصيد المحفظة:", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
                            }
                            Text(
                                "250.00 ريال",
                                color = AppTheme.colors.appleGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = AppTheme.colors.border)

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Menu Items
                DrawerItem(
                    icon = Icons.Default.Person,
                    label = "الحساب الشخصي",
                    tag = "drawer_account",
                    tint = AppTheme.colors.textPrimary,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToAccount()
                    }
                )

                DrawerItem(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    label = "سجل الرحلات والفواتير",
                    tag = "drawer_history",
                    tint = AppTheme.colors.textPrimary,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToHistory()
                    }
                )

                DrawerItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "المحفظة وطرق الدفع",
                    tag = "drawer_wallet",
                    tint = AppTheme.colors.textPrimary,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToAccount()
                    }
                )

                DrawerItem(
                    icon = Icons.Default.SupportAgent,
                    label = "الدعم الفني والمساعدة",
                    tag = "drawer_support",
                    tint = AppTheme.colors.textPrimary,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToSupport()
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Security,
                    label = "مركز الأمان وطوارئ SOS",
                    tag = "drawer_safety",
                    tint = AppTheme.colors.textPrimary,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        Toast.makeText(context, "تم تفعيل حماية الأمان ومشاركة الموقع المباشر", Toast.LENGTH_SHORT).show()
                        onNavigateToSupport()
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(color = AppTheme.colors.border)

                // Logout Item
                DrawerItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    label = "تسجيل الخروج",
                    tag = "drawer_logout",
                    tint = AppTheme.colors.rose,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        Toast.makeText(context, "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()
                        onNavigateBackToLogin()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "بوابة الراكب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            modifier = Modifier.testTag("menu_button")
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "القائمة",
                                tint = AppTheme.colors.appleGreen
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { ThemeManager.toggleTheme() },
                            modifier = Modifier.testTag("passenger_theme_toggle")
                        ) {
                            Icon(
                                if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "تبديل المظهر",
                                tint = AppTheme.colors.appleGreen
                            )
                        }
                        IconButton(
                            onClick = onNavigateBackToLogin,
                            modifier = Modifier.testTag("back_button")
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // If a trip is currently active, show a Live Status Banner
                if (liveState.isCounting || liveState.status == TripStatus.RUNNING || liveState.status == TripStatus.STOPPED) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLiveTrip() }
                            .padding(bottom = 24.dp)
                            .testTag("active_trip_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(AppTheme.colors.appleGreen, AppTheme.colors.primary))
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
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.appleGreen)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "رحلة جارية الآن: ${liveState.currentTripId}",
                                        color = AppTheme.colors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "المجموع الحالي: ${String.format(java.util.Locale.US, "%.2f", liveState.totalFare)} ريال",
                                        color = AppTheme.colors.appleGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                "عرض العداد >",
                                color = AppTheme.colors.appleGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "بدء رحلة جديدة مع السائق",
                    color = AppTheme.colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "اختر مسح الباركود المعروض لدى السائق أو أدخل رقم الرحلة",
                    color = AppTheme.colors.textSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // PRIMARY BUTTON 1: "امسح الباركود" (Scan Barcode)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x332563EB))
                        .clickable {
                            showScannerDialog = true
                        }
                        .testTag("scan_qr_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF38BDF8))),
                        width = 1.5.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    if (isDarkMode) {
                                        listOf(
                                            Color(0xFF0E2A68).copy(alpha = 0.4f),
                                            AppTheme.colors.surface
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFFEFF6FF),
                                            AppTheme.colors.surface
                                        )
                                    }
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "امسح الباركود (QR)",
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "وجّه الكاميرا إلى شاشة السائق لبدء الربط المباشر",
                                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1D4ED8), Color(0xFF2563EB))
                                        )
                                    )
                                    .border(2.dp, Color(0xFF38BDF8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = "مسح الباركود",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // PRIMARY BUTTON 2: "ادخل رقم الرحلة" (Enter Trip Number)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x221E3A8A))
                        .clickable {
                            inputTripId = liveState.currentTripId
                            showEnterTripIdDialog = true
                        }
                        .testTag("enter_trip_id_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF1F3F7A), Color(0xFF2563EB).copy(alpha = 0.5f))),
                        width = 1.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    if (isDarkMode) {
                                        listOf(
                                            AppTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                                            AppTheme.colors.surface
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFFF8FAFC),
                                            AppTheme.colors.surface
                                        )
                                    }
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "أدخل رقم الرحلة",
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "أدخل الكود المكون من حروف وأرقام مثل (#TRIP-4921)",
                                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkMode) Color(0xFF132349) else Color(0xFFDBEAFE))
                                    .border(1.5.dp, Color(0xFF2563EB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Numbers,
                                    contentDescription = "رقم الرحلة",
                                    tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF1D4ED8),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // QR SCANNER MODAL DIALOG
    if (showScannerDialog) {
        AlertDialog(
            onDismissRequest = { showScannerDialog = false },
            containerColor = AppTheme.colors.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = AppTheme.colors.appleGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مسح باركود الرحلة", color = AppTheme.colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { isFlashlightOn = !isFlashlightOn }) {
                        Icon(
                            if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "كشاف",
                            tint = if (isFlashlightOn) AppTheme.colors.appleGreen else AppTheme.colors.textMuted
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "ضع رمز الـ QR المعروض على شاشة السائق داخل الإطار",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scanner Viewfinder with Laser
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clickable {
                                // Auto scan trigger simulation / Direct connect
                                coroutineScope.launch {
                                    showScannerDialog = false
                                    val targetId = liveState.currentTripId.ifBlank { "TRIP-LIVE" }
                                    // Start passenger sync with driver server
                                    syncManager.startPassengerSync(
                                        meterManager = meterManager,
                                        tripId = targetId,
                                        driverIp = "127.0.0.1"
                                    )
                                    Toast.makeText(context, "تم مسح الباركود وربط العداد: $targetId", Toast.LENGTH_SHORT).show()
                                    onNavigateToFareApproval(targetId)
                                }
                            }
                    ) {
                        QrScannerViewfinder()
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Or enter barcode payload manually
                    OutlinedTextField(
                        value = manualBarcodeText,
                        onValueChange = { manualBarcodeText = it },
                        placeholder = { Text("أو الصق نص الباركود هنا...", color = AppTheme.colors.textMuted, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.appleGreen,
                            unfocusedBorderColor = AppTheme.colors.border,
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showScannerDialog = false
                        val payload = manualBarcodeText.trim().ifBlank { liveState.qrPayload.ifBlank { liveState.currentTripId.ifBlank { "TRIP-LIVE" } } }
                        meterManager.applyTripPayload(payload)
                        val tripId = meterManager.liveState.value.currentTripId.ifBlank { "TRIP-LIVE" }

                        // Extract IP if in payload
                        var driverIp = "127.0.0.1"
                        if (payload.contains("|IP:")) {
                            val ip = payload.substringAfter("|IP:").substringBefore("|")
                            if (ip.isNotBlank()) driverIp = ip
                        }

                        // Connect sync
                        syncManager.startPassengerSync(
                            meterManager = meterManager,
                            tripId = tripId,
                            driverIp = driverIp
                        )

                        Toast.makeText(context, "تم ربط العداد بنجاح: $tripId", Toast.LENGTH_SHORT).show()
                        onNavigateToFareApproval(tripId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.appleGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تأكيد وربط الباركود", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScannerDialog = false }) {
                    Text("إلغاء", color = AppTheme.colors.textMuted)
                }
            }
        )
    }

    // ENTER TRIP ID DIALOG
    if (showEnterTripIdDialog) {
        AlertDialog(
            onDismissRequest = { showEnterTripIdDialog = false },
            containerColor = AppTheme.colors.surface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Numbers, contentDescription = null, tint = AppTheme.colors.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إدخال رقم الرحلة", color = AppTheme.colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "أدخل رقم الرحلة المعروض على شاشة هاتف السائق:",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = inputTripId,
                        onValueChange = { inputTripId = it.uppercase() },
                        placeholder = { Text("مثال: TRIP-4921", color = AppTheme.colors.textMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trip_id_text_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.primary,
                            unfocusedBorderColor = AppTheme.colors.border,
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary,
                            focusedContainerColor = AppTheme.colors.bg,
                            unfocusedContainerColor = AppTheme.colors.bg
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Paste active trip ID if available
                    if (liveState.currentTripId.isNotBlank()) {
                        TextButton(
                            onClick = { inputTripId = liveState.currentTripId },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("استخدام رقم الرحلة (${liveState.currentTripId})", color = AppTheme.colors.appleGreen, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanId = inputTripId.trim().uppercase()
                        if (cleanId.isNotBlank()) {
                            showEnterTripIdDialog = false
                            meterManager.applyTripPayload(cleanId)

                            // Start passenger sync for this trip ID across devices
                            syncManager.startPassengerSync(
                                meterManager = meterManager,
                                tripId = cleanId,
                                driverIp = "127.0.0.1"
                            )

                            Toast.makeText(context, "تم ربط الجلسة برقم: $cleanId", Toast.LENGTH_SHORT).show()
                            onNavigateToFareApproval(cleanId)
                        } else {
                            Toast.makeText(context, "يرجى كتابة رقم الرحلة", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_trip_id_button")
                ) {
                    Text("ربط وبدء الجلسة", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnterTripIdDialog = false }) {
                    Text("إلغاء", color = AppTheme.colors.textMuted)
                }
            }
        )
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    tag: String,
    tint: Color,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp)) },
        label = { Text(label, color = tint, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
        selected = false,
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag(tag),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent
        )
    )
}
