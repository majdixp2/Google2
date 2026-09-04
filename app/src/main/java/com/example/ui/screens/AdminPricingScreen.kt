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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TripRepository
import com.example.model.DriverRecord
import com.example.model.MeterMode
import com.example.model.PassengerRecord
import com.example.model.TariffConfig
import com.example.model.TripRecord
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPricingScreen(
    repository: TripRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tariffState by repository.tariffConfig.collectAsState(initial = null)
    val allTrips by repository.allTrips.collectAsState(initial = emptyList())
    val allDrivers by repository.allDrivers.collectAsState(initial = emptyList())
    val allPassengers by repository.allPassengers.collectAsState(initial = emptyList())
    val allDisputes by repository.allDisputes.collectAsState(initial = emptyList())

    val currentTariff = tariffState ?: TariffConfig()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("التسعيرات والرسوم", "سجلات السائقين", "سجلات الركاب", "التحديث الإجباري")

    // Pricing & Fees Local Form States
    var extraBaseFare by remember(currentTariff) { mutableStateOf(currentTariff.extraRideBaseFare.toString()) }
    var extraPerKm by remember(currentTariff) { mutableStateOf(currentTariff.extraRidePerKm.toString()) }
    var extraPerMin by remember(currentTariff) { mutableStateOf(currentTariff.extraRidePerMin.toString()) }
    var waitingBaseFare by remember(currentTariff) { mutableStateOf(currentTariff.waitingBaseFare.toString()) }
    var waitingPerMin by remember(currentTariff) { mutableStateOf(currentTariff.waitingPerMin.toString()) }
    var taxPercentage by remember(currentTariff) { mutableStateOf(currentTariff.taxPercentage.toString()) }

    // Platform Fee States
    var platformFeeEnabled by remember(currentTariff) { mutableStateOf(currentTariff.platformFeeEnabled) }
    var platformFeePercentage by remember(currentTariff) { mutableStateOf(currentTariff.platformFeePercentage.toString()) }
    var platformFixedFee by remember(currentTariff) { mutableStateOf(currentTariff.platformFixedFee.toString()) }

    // Force Update Form States
    var forceUpdateEnabled by remember(currentTariff) { mutableStateOf(currentTariff.forceUpdateEnabled) }
    var minVersionCode by remember(currentTariff) { mutableStateOf(currentTariff.minAppVersionCode.toString()) }
    var latestVersionName by remember(currentTariff) { mutableStateOf(currentTariff.latestVersionName) }
    var forceUpdateTitle by remember(currentTariff) { mutableStateOf(currentTariff.forceUpdateTitle) }
    var forceUpdateMessage by remember(currentTariff) { mutableStateOf(currentTariff.forceUpdateMessage) }
    var updateDownloadUrl by remember(currentTariff) { mutableStateOf(currentTariff.updateDownloadUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "لوحة تحكم وإدارة النظام المركزية",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PolishTextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = PolishTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PolishSurface),
                modifier = Modifier.border(1.dp, PolishBorder)
            )
        },
        containerColor = PolishBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Tab Navigation Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = PolishSurface,
                contentColor = PolishPrimary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PolishPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier.border(1.dp, PolishBorder)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedTabIndex == index),
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == index) PolishPrimary else PolishTextSecondary
                            )
                        }
                    )
                }
            }

            // Tab Content Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> PricingAndFeesTab(
                        extraBaseFare = extraBaseFare,
                        onExtraBaseChange = { extraBaseFare = it },
                        extraPerKm = extraPerKm,
                        onExtraKmChange = { extraPerKm = it },
                        extraPerMin = extraPerMin,
                        onExtraMinChange = { extraPerMin = it },
                        waitingBaseFare = waitingBaseFare,
                        onWaitingBaseChange = { waitingBaseFare = it },
                        waitingPerMin = waitingPerMin,
                        onWaitingMinChange = { waitingPerMin = it },
                        taxPercentage = taxPercentage,
                        onTaxChange = { taxPercentage = it },
                        platformFeeEnabled = platformFeeEnabled,
                        onPlatformFeeEnabledChange = { platformFeeEnabled = it },
                        platformFeePercentage = platformFeePercentage,
                        onPlatformFeePercentageChange = { platformFeePercentage = it },
                        platformFixedFee = platformFixedFee,
                        onPlatformFixedFeeChange = { platformFixedFee = it },
                        onResetDefaults = {
                            extraBaseFare = "5.0"
                            extraPerKm = "2.0"
                            extraPerMin = "0.5"
                            waitingBaseFare = "3.0"
                            waitingPerMin = "1.0"
                            taxPercentage = "15.0"
                            platformFeeEnabled = false
                            platformFeePercentage = "10.0"
                            platformFixedFee = "0.0"
                            Toast.makeText(context, "تمت استعادة الأسعار والرسوم الافتراضية", Toast.LENGTH_SHORT).show()
                        },
                        onSave = {
                            coroutineScope.launch {
                                val updated = currentTariff.copy(
                                    extraRideBaseFare = extraBaseFare.toDoubleOrNull() ?: 5.0,
                                    extraRidePerKm = extraPerKm.toDoubleOrNull() ?: 2.0,
                                    extraRidePerMin = extraPerMin.toDoubleOrNull() ?: 0.5,
                                    waitingBaseFare = waitingBaseFare.toDoubleOrNull() ?: 3.0,
                                    waitingPerMin = waitingPerMin.toDoubleOrNull() ?: 1.0,
                                    taxPercentage = taxPercentage.toDoubleOrNull() ?: 15.0,
                                    platformFeeEnabled = platformFeeEnabled,
                                    platformFeePercentage = platformFeePercentage.toDoubleOrNull() ?: 10.0,
                                    platformFixedFee = platformFixedFee.toDoubleOrNull() ?: 0.0
                                )
                                repository.updateTariff(updated)
                                Toast.makeText(context, "تم حفظ التسعيرات والرسوم بنجاح وتعميمها على العداد!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    1 -> DriverRecordsTab(
                        drivers = allDrivers,
                        allTrips = allTrips
                    )

                    2 -> PassengerRecordsTab(
                        passengers = allPassengers,
                        allTrips = allTrips,
                        allDisputes = allDisputes
                    )

                    3 -> ForceUpdateTab(
                        forceUpdateEnabled = forceUpdateEnabled,
                        onForceUpdateEnabledChange = { forceUpdateEnabled = it },
                        minVersionCode = minVersionCode,
                        onMinVersionCodeChange = { minVersionCode = it },
                        latestVersionName = latestVersionName,
                        onLatestVersionNameChange = { latestVersionName = it },
                        forceUpdateTitle = forceUpdateTitle,
                        onForceUpdateTitleChange = { forceUpdateTitle = it },
                        forceUpdateMessage = forceUpdateMessage,
                        onForceUpdateMessageChange = { forceUpdateMessage = it },
                        updateDownloadUrl = updateDownloadUrl,
                        onUpdateDownloadUrlChange = { updateDownloadUrl = it },
                        onSaveForceUpdate = {
                            coroutineScope.launch {
                                val updated = currentTariff.copy(
                                    forceUpdateEnabled = forceUpdateEnabled,
                                    minAppVersionCode = minVersionCode.toIntOrNull() ?: 1,
                                    latestVersionName = latestVersionName.ifBlank { "2.5.0" },
                                    forceUpdateTitle = forceUpdateTitle.ifBlank { "تحديث إلزامي ومطلوب للخدمة" },
                                    forceUpdateMessage = forceUpdateMessage.ifBlank { "يتوجب عليك تحديث التطبيق للمتابعة" },
                                    updateDownloadUrl = updateDownloadUrl.ifBlank { "https://play.google.com/store/apps" }
                                )
                                repository.updateTariff(updated)
                                Toast.makeText(
                                    context,
                                    if (forceUpdateEnabled) "تم تفعيل التحديث الإجباري لجميع الأجهزة!" else "تم إيقاف التحديث الإجباري",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: PRICING & FUTURE PLATFORM FEES
// ----------------------------------------------------
@Composable
private fun PricingAndFeesTab(
    extraBaseFare: String,
    onExtraBaseChange: (String) -> Unit,
    extraPerKm: String,
    onExtraKmChange: (String) -> Unit,
    extraPerMin: String,
    onExtraMinChange: (String) -> Unit,
    waitingBaseFare: String,
    onWaitingBaseChange: (String) -> Unit,
    waitingPerMin: String,
    onWaitingMinChange: (String) -> Unit,
    taxPercentage: String,
    onTaxChange: (String) -> Unit,
    platformFeeEnabled: Boolean,
    onPlatformFeeEnabledChange: (Boolean) -> Unit,
    platformFeePercentage: String,
    onPlatformFeePercentageChange: (String) -> Unit,
    platformFixedFee: String,
    onPlatformFixedFeeChange: (String) -> Unit,
    onResetDefaults: () -> Unit,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Extra Ride Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(PolishPrimary, PolishPrimaryLight))
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PolishPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Route, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("تسعيرة المشوار الإضافي (Extra Ride)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PolishTextPrimary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminField(
                        label = "فتح العداد (ريال)",
                        value = extraBaseFare,
                        onValueChange = onExtraBaseChange,
                        modifier = Modifier.weight(1f)
                    )
                    AdminField(
                        label = "لكل 1 كم (ريال)",
                        value = extraPerKm,
                        onValueChange = onExtraKmChange,
                        modifier = Modifier.weight(1f)
                    )
                    AdminField(
                        label = "لكل دقيقة (ريال)",
                        value = extraPerMin,
                        onValueChange = onExtraMinChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Paid Waiting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(PolishSecondary, PolishSecondaryLight))
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PolishSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = PolishSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("تسعيرة الانتظار المدفوع (Paid Waiting)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PolishTextPrimary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminField(
                        label = "فتح العداد للانتظار (ريال)",
                        value = waitingBaseFare,
                        onValueChange = onWaitingBaseChange,
                        modifier = Modifier.weight(1f)
                    )
                    AdminField(
                        label = "لكل دقيقة انتظار (ريال)",
                        value = waitingPerMin,
                        onValueChange = onWaitingMinChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Future Platform Fees & Commission Card (فرض رسوم لاحقاً)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(PolishIndigo, PolishIndigoLight))
            )
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PolishIndigoContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Percent, contentDescription = null, tint = PolishIndigo, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("رسوم وعمولة المنصة (مستقبلاً)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PolishTextPrimary)
                            Text("التحكم بفرض واقتطاع رسوم الخدمة من الرحلات", fontSize = 11.sp, color = PolishTextSecondary)
                        }
                    }

                    Switch(
                        checked = platformFeeEnabled,
                        onCheckedChange = onPlatformFeeEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PolishIndigo,
                            checkedTrackColor = PolishIndigoContainer
                        )
                    )
                }

                AnimatedVisibility(visible = platformFeeEnabled) {
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminField(
                                label = "نسبة عمولة المنصة (%)",
                                value = platformFeePercentage,
                                onValueChange = onPlatformFeePercentageChange,
                                modifier = Modifier.weight(1f)
                            )
                            AdminField(
                                label = "رسوم ثابتة لكل مشوار (ريال)",
                                value = platformFixedFee,
                                onValueChange = onPlatformFixedFeeChange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "ملاحظة: عند تفعيل الرسوم، يتم احتسابها تلقائياً وإظهار صافي أرباح السائق ورسوم التطبيق بشفافية تامة.",
                            fontSize = 11.sp,
                            color = PolishIndigo
                        )
                    }
                }
            }
        }

        // Taxes & VAT Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(PolishBorder, PolishBorderSubtle))
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("الضرائب والعملة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PolishTextPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminField(
                        label = "ضريبة القيمة المضافة VAT (%)",
                        value = taxPercentage,
                        onValueChange = onTaxChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Buttons Bar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onResetDefaults,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("admin_reset_pricing_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = PolishTextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("الافتراضيات", color = PolishTextSecondary, fontSize = 13.sp)
            }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp)
                    .testTag("admin_save_pricing_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ وتطبيق التسعيرات", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: DRIVER RECORDS & DETAILED TRIP LOGS
// ----------------------------------------------------
@Composable
private fun DriverRecordsTab(
    drivers: List<DriverRecord>,
    allTrips: List<TripRecord>
) {
    var expandedDriverId by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer.copy(alpha = 0.35f)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PolishPrimary, PolishPrimaryLight)))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("إجمالي أسطول السائقين المعتمدين", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PolishPrimary)
                    Text("${drivers.size} كابتن مسجل في النظام", fontSize = 12.sp, color = PolishTextSecondary)
                }
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(32.dp))
            }
        }

        // Driver List
        drivers.forEach { driver ->
            val driverTrips = allTrips.filter { it.driverId == driver.driverId || it.driverName.contains(driver.fullName) }
            val isExpanded = (expandedDriverId == driver.driverId)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedDriverId = if (isExpanded) null else driver.driverId
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (isExpanded) listOf(PolishPrimary, PolishSecondary) else listOf(PolishBorder, PolishBorderSubtle)
                    )
                )
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PolishPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(driver.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PolishTextPrimary)
                                Text("${driver.carModel} • لوحة: ${driver.carPlate}", fontSize = 12.sp, color = PolishTextSecondary)
                            }
                        }

                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = PolishTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("الرحلات", color = PolishTextSecondary, fontSize = 10.sp)
                            Text("${driver.totalTripsCount + driverTrips.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishTextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("إجمالي الدخل", color = PolishTextSecondary, fontSize = 10.sp)
                            Text("${String.format(java.util.Locale.US, "%.1f", driver.totalEarnings)} ر.س", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("التقييم", color = PolishTextSecondary, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = PolishAmber, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${driver.rating}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishTextPrimary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("الحالة", color = PolishTextSecondary, fontSize = 10.sp)
                            Text(driver.status, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PolishSecondary)
                        }
                    }

                    // Expanded Trip History for Driver
                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            HorizontalDivider(color = PolishBorderSubtle)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("سجل رحلات وانتظار السائق (${driverTrips.size} مسجلة):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishPrimary)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (driverTrips.isEmpty()) {
                                Text("لا توجد رحلات مكتملة مسجلة لهذا السائق حالياً.", fontSize = 12.sp, color = PolishTextMuted)
                            } else {
                                driverTrips.forEach { trip ->
                                    TripItemCompact(trip = trip)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: PASSENGER RECORDS & DETAILED TRIP LOGS
// ----------------------------------------------------
@Composable
private fun PassengerRecordsTab(
    passengers: List<PassengerRecord>,
    allTrips: List<TripRecord>,
    allDisputes: List<com.example.model.TripDispute>
) {
    var expandedPassengerId by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSecondaryContainer.copy(alpha = 0.35f)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PolishSecondary, PolishSecondaryLight)))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("سجلات وحسابات الركاب", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PolishSecondaryDark)
                    Text("${passengers.size} راكب نشط وموثق في المنظومة", fontSize = 12.sp, color = PolishTextSecondary)
                }
                Icon(Icons.Default.People, contentDescription = null, tint = PolishSecondary, modifier = Modifier.size(32.dp))
            }
        }

        // Passenger List
        passengers.forEach { passenger ->
            val passengerTrips = allTrips.filter { it.passengerId == passenger.passengerId || it.passengerName.contains(passenger.fullName) }
            val isExpanded = (expandedPassengerId == passenger.passengerId)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedPassengerId = if (isExpanded) null else passenger.passengerId
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (isExpanded) listOf(PolishSecondary, PolishPrimary) else listOf(PolishBorder, PolishBorderSubtle)
                    )
                )
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PolishSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PolishSecondary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(passenger.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PolishTextPrimary)
                                Text("${passenger.phone} • ${passenger.email}", fontSize = 11.sp, color = PolishTextSecondary)
                            }
                        }

                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = PolishTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("المشاوير", color = PolishTextSecondary, fontSize = 10.sp)
                            Text("${passenger.totalTripsCount + passengerTrips.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishTextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("رصيد المحفظة", color = PolishTextSecondary, fontSize = 10.sp)
                            Text("${String.format(java.util.Locale.US, "%.1f", passenger.walletBalance)} ر.س", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("الاعتراضات", color = PolishTextSecondary, fontSize = 10.sp)
                            Text("${passenger.disputesCount}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (passenger.disputesCount > 0) PolishRose else PolishTextPrimary)
                        }
                    }

                    // Expanded Trip History for Passenger
                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            HorizontalDivider(color = PolishBorderSubtle)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("سجل رحلات وانتظار الراكب (${passengerTrips.size} مشوار):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishSecondaryDark)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (passengerTrips.isEmpty()) {
                                Text("لا توجد رحلات سابقة مسجلة لهذا الراكب.", fontSize = 12.sp, color = PolishTextMuted)
                            } else {
                                passengerTrips.forEach { trip ->
                                    TripItemCompact(trip = trip)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 4: MANDATORY FORCE UPDATE MANAGEMENT
// ----------------------------------------------------
@Composable
private fun ForceUpdateTab(
    forceUpdateEnabled: Boolean,
    onForceUpdateEnabledChange: (Boolean) -> Unit,
    minVersionCode: String,
    onMinVersionCodeChange: (String) -> Unit,
    latestVersionName: String,
    onLatestVersionNameChange: (String) -> Unit,
    forceUpdateTitle: String,
    onForceUpdateTitleChange: (String) -> Unit,
    forceUpdateMessage: String,
    onForceUpdateMessageChange: (String) -> Unit,
    updateDownloadUrl: String,
    onUpdateDownloadUrlChange: (String) -> Unit,
    onSaveForceUpdate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Master Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    if (forceUpdateEnabled) listOf(PolishRose, PolishRoseLight) else listOf(PolishBorder, PolishBorderSubtle)
                )
            )
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (forceUpdateEnabled) PolishRoseContainer else PolishPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = if (forceUpdateEnabled) PolishRose else PolishPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("تفعيل التحديث الإجباري لجميع التطبيقات", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PolishTextPrimary)
                            Text(
                                if (forceUpdateEnabled) "النظام مفعل حالياً (يمنع استخدام النسخ القديمة)" else "معطل (التطبيق يعمل لجميع المستخدمين)",
                                fontSize = 11.sp,
                                color = if (forceUpdateEnabled) PolishRose else PolishTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = forceUpdateEnabled,
                        onCheckedChange = onForceUpdateEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PolishRose,
                            checkedTrackColor = PolishRoseContainer
                        )
                    )
                }
            }
        }

        // Version & Store Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PolishBorder, PolishBorderSubtle)))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إعدادات النسخة والمتجر", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PolishTextPrimary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminField(
                        label = "أدنى رقم إصدار مطلوب (Code)",
                        value = minVersionCode,
                        onValueChange = onMinVersionCodeChange,
                        modifier = Modifier.weight(1f)
                    )
                    AdminField(
                        label = "رقم النسخة الأحدث (Version)",
                        value = latestVersionName,
                        onValueChange = onLatestVersionNameChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                AdminField(
                    label = "عنوان رسالة التحديث الإجباري",
                    value = forceUpdateTitle,
                    onValueChange = onForceUpdateTitleChange
                )

                OutlinedTextField(
                    value = forceUpdateMessage,
                    onValueChange = onForceUpdateMessageChange,
                    label = { Text("نص الرسالة والتنبيه للراكب والسائق", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishBorder
                    )
                )

                AdminField(
                    label = "رابط متجر Google Play أو التحميل المباشر",
                    value = updateDownloadUrl,
                    onValueChange = onUpdateDownloadUrlChange
                )
            }
        }

        // Save Button
        Button(
            onClick = onSaveForceUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("admin_save_force_update_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (forceUpdateEnabled) PolishRose else PolishPrimary
            )
        ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("حفظ إعدادات التحديث الإجباري", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

// ----------------------------------------------------
// COMPACT TRIP ITEM FOR DETAILED LOGS
// ----------------------------------------------------
@Composable
private fun TripItemCompact(trip: TripRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PolishBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PolishBorder, PolishBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (trip.mode == MeterMode.EXTRA_RIDE) PolishPrimaryContainer else PolishSecondaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            trip.mode.titleAr,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (trip.mode == MeterMode.EXTRA_RIDE) PolishPrimary else PolishSecondaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(trip.tripId, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PolishTextPrimary)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${String.format(java.util.Locale.US, "%.1f", trip.distanceMeters / 1000.0)} كم • ${trip.durationSeconds / 60} دقيقة • ${trip.createdAtFormatted}",
                    fontSize = 10.sp,
                    color = PolishTextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${String.format(java.util.Locale.US, "%.2f", trip.totalFare)} ر.س",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = PolishPrimary
                )
                if (trip.isDisputed) {
                    Text("تم تدقيق المسار", fontSize = 9.sp, color = PolishRose, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PolishPrimary,
            unfocusedBorderColor = PolishBorder,
            focusedTextColor = PolishTextPrimary,
            unfocusedTextColor = PolishTextPrimary
        ),
        singleLine = true
    )
}
