package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.TariffConfig
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSecondary
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun ForceUpdateDialog(
    tariffConfig: TariffConfig,
    onAdminBypass: () -> Unit = {}
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = { /* Non dismissible */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("force_update_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = PolishSurface,
            tonalElevation = 8.dp,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(PolishPrimary, PolishPrimary.copy(alpha = 0.3f))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(PolishPrimaryContainer, PolishPrimary.copy(alpha = 0.15f))
                            )
                        )
                        .border(2.dp, PolishPrimary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SystemUpdate,
                        contentDescription = "تحديث إجباري",
                        tint = PolishPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PolishPrimaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "الإصدار الإلزامي الجديد v${tariffConfig.latestVersionName}",
                            color = PolishPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tariffConfig.forceUpdateTitle.ifBlank { "تحديث إلزامي ومطلوب للخدمة" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PolishTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = tariffConfig.forceUpdateMessage.ifBlank {
                        "يتوجب عليك تحديث تطبيق عداد المشاوير الآن لضمان دقة التسعير والتوافق مع مسارات وتوجيهات الهيئة العامة للنقل."
                    },
                    fontSize = 14.sp,
                    color = PolishTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Highlights Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer.copy(alpha = 0.35f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(PolishBorder, PolishBorder))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "ما الجديد في هذا التحديث:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PolishPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• تدقيق آلي للمسارات ومقارنتها بدقة مع Google Maps", fontSize = 12.sp, color = PolishTextSecondary)
                        Text("• حماية الراكب من انحراف السائق عن المسار المباشر", fontSize = 12.sp, color = PolishTextSecondary)
                        Text("• تحسين استقرار نظام تسعير الكيلومتر والانتظار", fontSize = 12.sp, color = PolishTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Primary Update Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tariffConfig.updateDownloadUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store"))
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(webIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("force_update_download_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "تحديث التطبيق الآن",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onAdminBypass,
                    modifier = Modifier.testTag("force_update_bypass_button")
                ) {
                    Text(
                        "الدخول كمسؤول للنظام (Admin)",
                        color = PolishTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
