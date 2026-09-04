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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginAsPassenger: () -> Unit,
    onNavigateToDriver: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    var email by remember { mutableStateOf("passenger@smartmeter.com") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isBiometricAuthenticating by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var biometricSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = AppTheme.colors.appleGreen,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "عداد المشاوير الذكي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { ThemeManager.toggleTheme() },
                        modifier = Modifier.testTag("login_theme_toggle")
                    ) {
                        Icon(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تبديل المظهر",
                            tint = AppTheme.colors.appleGreen
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.testTag("admin_button")
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "لوحة تحكم الأسعار",
                            tint = AppTheme.colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surface,
                    titleContentColor = AppTheme.colors.textPrimary
                ),
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
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Hero Visual Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(AppTheme.colors.border, AppTheme.colors.borderSubtle))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مرحباً بك كـ راكب",
                            color = AppTheme.colors.appleGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تسجيل الدخول ومسح باركود الرحلات والعداد المباشر",
                            color = AppTheme.colors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "راكب",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Email Input Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("البريد الإلكتروني") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = AppTheme.colors.appleGreen)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.appleGreen,
                    unfocusedBorderColor = AppTheme.colors.border,
                    focusedLabelColor = AppTheme.colors.appleGreen,
                    unfocusedLabelColor = AppTheme.colors.textMuted,
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    focusedContainerColor = AppTheme.colors.surface,
                    unfocusedContainerColor = AppTheme.colors.surface
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AppTheme.colors.appleGreen)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "إخفاء" else "إظهار",
                            tint = AppTheme.colors.textMuted
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.appleGreen,
                    unfocusedBorderColor = AppTheme.colors.border,
                    focusedLabelColor = AppTheme.colors.appleGreen,
                    unfocusedLabelColor = AppTheme.colors.textMuted,
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    focusedContainerColor = AppTheme.colors.surface,
                    unfocusedContainerColor = AppTheme.colors.surface
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Login as Passenger Button
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        Toast.makeText(context, "تم تسجيل دخول الراكب بنجاح", Toast.LENGTH_SHORT).show()
                        onLoginAsPassenger()
                    } else {
                        Toast.makeText(context, "يرجى كتابة البريد الإلكتروني", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0x662563EB))
                    .testTag("passenger_login_button"),
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
                    Text(
                        "تسجيل الدخول كـ راكب",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Biometric Login Button
            OutlinedButton(
                onClick = {
                    showBiometricDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("biometric_login_button"),
                shape = RoundedCornerShape(18.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF38BDF8))),
                    width = 1.5.dp
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AppTheme.colors.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = "بصمة الإصبع والوجه",
                    tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF2563EB),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "الدخول بالمقاييس الحيوية (بصمة / وجه)",
                    color = AppTheme.colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Divider and Driver Registration/Login Link at Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppTheme.colors.border)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "هل أنت كابتن سائق وتريد بدء تشغيل العداد؟",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToDriver,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("driver_register_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AppTheme.colors.surface
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF0284C7))),
                    width = 2.dp
                )
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF2563EB),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "سجل كسائق / الدخول كـ سائق",
                    color = if (isDarkMode) Color.White else Color(0xFF1E3A8A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Biometric Simulation Dialog
    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricDialog = false },
            containerColor = AppTheme.colors.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = AppTheme.colors.appleGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("المقاييس الحيوية للأمان", color = AppTheme.colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        "يرجى لمس مستشعر البصمة أو مطابقة ملامح الوجه لتسجيل الدخول السريع",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (biometricSuccess) AppTheme.colors.appleGreenContainer
                                else AppTheme.colors.primaryContainer
                            )
                            .border(
                                2.dp,
                                if (biometricSuccess) AppTheme.colors.appleGreen else AppTheme.colors.primary,
                                CircleShape
                            )
                            .clickable {
                                isBiometricAuthenticating = true
                                coroutineScope.launch {
                                    delay(1000)
                                    isBiometricAuthenticating = false
                                    biometricSuccess = true
                                    delay(600)
                                    showBiometricDialog = false
                                    Toast.makeText(context, "تم التحقق من المقاييس الحيوية بنجاح!", Toast.LENGTH_SHORT).show()
                                    onLoginAsPassenger()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBiometricAuthenticating) {
                            CircularProgressIndicator(color = AppTheme.colors.appleGreen, modifier = Modifier.size(36.dp))
                        } else {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = "بصمة",
                                tint = if (biometricSuccess) AppTheme.colors.appleGreen else AppTheme.colors.primary,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (biometricSuccess) "تم التحقق بنجاح!" else "اضغط على المستشعر للتحقق",
                        color = if (biometricSuccess) AppTheme.colors.appleGreen else AppTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isBiometricAuthenticating = true
                        coroutineScope.launch {
                            delay(800)
                            showBiometricDialog = false
                            onLoginAsPassenger()
                        }
                    }
                ) {
                    Text("تأكيد الدخول", color = AppTheme.colors.appleGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricDialog = false }) {
                    Text("إلغاء", color = AppTheme.colors.textMuted)
                }
            }
        )
    }
}
