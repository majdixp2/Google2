package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeManager

data class SupportMessage(
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val time: String = "الآن"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val messages = remember {
        mutableStateListOf(
            SupportMessage(
                sender = "فريق الدعم الفني",
                text = "مرحباً بك في خدمة الدعم الفني لعداد المشاوير الذكي! كيف يمكننا مساعدتك اليوم بخصوص حساب الرحلات أو العداد أو الأسعار؟",
                isUser = false
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = AppTheme.colors.appleGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "الدعم الفني والمساعدة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("support_back_button")
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
                        modifier = Modifier.testTag("support_theme_toggle")
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
                .padding(16.dp)
        ) {

            // Emergency SOS & Direct Call Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            Toast.makeText(context, "تم إرسال إشعار الطوارئ ومشاركة الموقع!", Toast.LENGTH_SHORT).show()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.rose.copy(alpha = 0.1f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppTheme.colors.rose.copy(alpha = 0.4f), AppTheme.colors.rose.copy(alpha = 0.4f)))),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = null, tint = AppTheme.colors.rose, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("طوارئ SOS", color = AppTheme.colors.rose, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            Toast.makeText(context, "جاري الاتصال بخط المساعدة المباشر 920000000", Toast.LENGTH_SHORT).show()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primaryContainer),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AppTheme.colors.primary.copy(alpha = 0.3f), AppTheme.colors.primary.copy(alpha = 0.3f)))),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = AppTheme.colors.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اتصال مباشر", color = AppTheme.colors.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chat Messages Container
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                messages.forEach { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.Start else Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (msg.isUser) AppTheme.colors.appleGreen else AppTheme.colors.bg)
                                .border(1.dp, if (msg.isUser) AppTheme.colors.appleGreen else AppTheme.colors.border, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    msg.sender,
                                    color = if (msg.isUser) Color.White.copy(alpha = 0.85f) else AppTheme.colors.appleGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    msg.text,
                                    color = if (msg.isUser) Color.White else AppTheme.colors.textPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Send Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("اكتب استفسارك هنا...", color = AppTheme.colors.textMuted, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("support_input_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.colors.appleGreen,
                        unfocusedBorderColor = AppTheme.colors.border,
                        focusedTextColor = AppTheme.colors.textPrimary,
                        unfocusedTextColor = AppTheme.colors.textPrimary,
                        focusedContainerColor = AppTheme.colors.surface,
                        unfocusedContainerColor = AppTheme.colors.surface
                    )
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userMsg = inputText.trim()
                            messages.add(SupportMessage("أنت", userMsg, true))
                            inputText = ""
                            // Automated Assistant Reply
                            val botReply = when {
                                userMsg.contains("باركود") || userMsg.contains("qr") ->
                                    "يمكنك مسح الباركود المعروض على هاتف السائق بالضغط على زر 'امسح الباركود' في الشاشة الرئيسية ثم قبول التسعيرة."
                                userMsg.contains("سعر") || userMsg.contains("تسعير") ->
                                    "يتم تحديد أسعار فتح العداد وسعر الكيلومتر ودقيقة الانتظار مركزياً من قِبل إدارة النظام وتظهر لك تفاصيلها قبل الموافقة وبدء العد."
                                userMsg.contains("سائق") ->
                                    "إذا كنت كابتن سائق، يمكنك تسجيل الدخول كسائق من أسفل صفحة الدخول واختيار نمط الانتظار أو المشوار الإضافي."
                                else ->
                                    "شكراً لتواصلك معنا! تم تسجيل طلبك وسيقوم ممثل خدمة العملاء بمتابعة الرحلة وتقديم المساعدة الفورية."
                            }
                            messages.add(SupportMessage("الدعم الفني الذكي", botReply, false))
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.appleGreen)
                        .testTag("support_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.White)
                }
            }
        }
    }
}
