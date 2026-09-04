package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MeterMode
import com.example.model.TripRecord
import com.example.ui.theme.*
import com.example.ui.theme.PolishSecondaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDisputeSheet(
    trip: TripRecord,
    onDismiss: () -> Unit,
    onSubmitDispute: (
        reasonCategory: String,
        passengerNotes: String,
        recordedDistanceKm: Double,
        idealDistanceKm: Double,
        excessDistanceKm: Double,
        excessPercentage: Int,
        originalFare: Double,
        correctedFare: Double,
        refundAmount: Double,
        googleMapsUrl: String
    ) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Calculate Route & Map Audit Metrics
    val recordedDistKm = String.format(java.util.Locale.US, "%.2f", trip.distanceMeters / 1000.0).toDoubleOrNull() ?: 0.0
    val idealDistKm = if (trip.idealDistanceMeters > 0) {
        String.format(java.util.Locale.US, "%.2f", trip.idealDistanceMeters / 1000.0).toDoubleOrNull() ?: 0.0
    } else if (recordedDistKm > 1.0) {
        String.format(java.util.Locale.US, "%.2f", recordedDistKm * 0.80).toDoubleOrNull() ?: recordedDistKm
    } else {
        recordedDistKm
    }

    val excessDistKm = if (recordedDistKm > idealDistKm) {
        String.format(java.util.Locale.US, "%.2f", recordedDistKm - idealDistKm).toDoubleOrNull() ?: 0.0
    } else {
        0.0
    }

    val excessPercentage = if (idealDistKm > 0 && excessDistKm > 0) {
        ((excessDistKm / idealDistKm) * 100).toInt()
    } else {
        0
    }

    val recordedDurationMin = trip.durationSeconds / 60
    val idealDurationMin = if (trip.idealDurationSeconds > 0) {
        trip.idealDurationSeconds / 60
    } else {
        (recordedDurationMin * 0.85).toLong().coerceAtLeast(1)
    }

    // Calculated Refund for Excess Route
    val estimatedExcessCost = if (trip.mode == MeterMode.EXTRA_RIDE && excessDistKm > 0.3) {
        (excessDistKm * 2.00) + ((recordedDurationMin - idealDurationMin).coerceAtLeast(0) * 0.50)
    } else if (trip.mode == MeterMode.PAID_WAITING) {
        trip.totalFare * 0.35
    } else {
        3.50
    }
    val refundAmount = String.format(java.util.Locale.US, "%.2f", estimatedExcessCost).toDoubleOrNull() ?: 5.00
    val correctedFare = String.format(java.util.Locale.US, "%.2f", (trip.totalFare - refundAmount).coerceAtLeast(trip.baseFare)).toDoubleOrNull() ?: trip.baseFare

    val startLat = if (trip.startLat != 0.0) trip.startLat else 24.7136
    val startLng = if (trip.startLng != 0.0) trip.startLng else 46.6753
    val endLat = if (trip.endLat != 0.0) trip.endLat else 24.8465
    val endLng = if (trip.endLng != 0.0) trip.endLng else 46.7329

    val googleMapsUrl = "https://www.google.com/maps/dir/?api=1&origin=$startLat,$startLng&destination=$endLat,$endLng&travelmode=driving"

    val disputeReasons = listOf(
        "أخطأ السائق في الطريق وسلك مساراً أطول تسبب بزيادة التكلفة",
        "توقفات وتأخير غير مبرر أثناء سير العداد",
        "عدم تطابق المسار الفعلي مع الوجهة المتفق عليها",
        "اعتراض على احتساب أجرة الانتظار",
        "أخرى (توضيح بالملاحظات)"
    )

    var selectedReason by remember { mutableStateOf(disputeReasons[0]) }
    var passengerNotes by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PolishSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
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
                            .background(PolishRoseContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ReportProblem,
                            contentDescription = null,
                            tint = PolishRose,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "اعتراض وتدقيق مسار الرحلة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PolishTextPrimary
                        )
                        Text(
                            "التحقق الذكي بالربط مع Google Maps",
                            fontSize = 11.sp,
                            color = PolishTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dispute_sheet_close_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = PolishTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trip Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PolishBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(PolishBorder, PolishBorder))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "رقم الرحلة: ${trip.tripId}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PolishPrimary
                        )
                        Text(
                            "المبلغ المسجل: ${String.format(java.util.Locale.US, "%.2f", trip.totalFare)} ريال",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PolishRose
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("السائق: ${trip.driverName} (${trip.driverCar})", fontSize = 12.sp, color = PolishTextSecondary)
                    Text("المسار: من ${trip.startLocationName} إلى ${trip.endLocationName}", fontSize = 11.sp, color = PolishTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Google Maps Verification & Deviation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PolishIndigoContainer.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(PolishIndigo, PolishIndigoLight))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = PolishIndigo, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نتيجة مطابقة مسار خرائط Google", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishIndigo)
                        }

                        if (excessPercentage > 10) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PolishRose)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("انحراف +$excessPercentage%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Comparison Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Recorded by meter
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PolishSurface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("المسجل بالعداد", color = PolishTextSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$recordedDistKm كم", color = PolishRose, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$recordedDurationMin دقيقة", color = PolishTextMuted, fontSize = 11.sp)
                            }
                        }

                        // Ideal by Google Maps
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PolishSurface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("المسار المباشر (Maps)", color = PolishTextSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$idealDistKm كم", color = PolishSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$idealDurationMin دقيقة قياسي", color = PolishTextMuted, fontSize = 11.sp)
                            }
                        }

                        // Extra Detour
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PolishSurface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("الزيادة غير المبررة", color = PolishTextSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("+$excessDistKm كم", color = PolishAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("فارق مسافة", color = PolishTextMuted, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Open in Google Maps Button
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح تطبيق Google Maps مباشرة", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("open_google_maps_audit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PolishIndigo),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فتح ومراجعة المسار على تطبيق Google Maps", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Refund Guarantee Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSecondaryContainer.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(PolishSecondary, PolishSecondaryLight))
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = PolishSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("التعويض المستحق فور قبول الاعتراض:", fontSize = 11.sp, color = PolishTextSecondary)
                        Text(
                            "${String.format(java.util.Locale.US, "%.2f", refundAmount)} ريال سعودي",
                            color = PolishSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text("يتم إيداعها مباشرة في رصيد محفظتك بعد المراجعة", fontSize = 10.sp, color = PolishTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reasons List
            Text(
                "حدد سبب الاعتراض بدقة:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PolishTextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            disputeReasons.forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedReason = reason }
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedReason == reason),
                        onClick = { selectedReason = reason },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = PolishPrimary,
                            unselectedColor = PolishBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        reason,
                        fontSize = 12.sp,
                        color = if (selectedReason == reason) PolishTextPrimary else PolishTextSecondary,
                        fontWeight = if (selectedReason == reason) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Passenger Note Input
            OutlinedTextField(
                value = passengerNotes,
                onValueChange = { passengerNotes = it },
                label = { Text("ملاحظات إضافية لتوضيح اعتراضك (اختياري)", fontSize = 12.sp) },
                placeholder = { Text("مثال: قام السائق بتغيير المسار دون الرجوع لي...", fontSize = 11.sp, color = PolishTextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("dispute_notes_field"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PolishPrimary,
                    unfocusedBorderColor = PolishBorder,
                    focusedTextColor = PolishTextPrimary,
                    unfocusedTextColor = PolishTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Dispute CTA
            Button(
                onClick = {
                    isSubmitted = true
                    onSubmitDispute(
                        selectedReason,
                        passengerNotes,
                        recordedDistKm,
                        idealDistKm,
                        excessDistKm,
                        excessPercentage,
                        trip.totalFare,
                        correctedFare,
                        refundAmount,
                        googleMapsUrl
                    )
                    Toast.makeText(
                        context,
                        "تم تسجيل اعتراضك وتدقيق المسار، وتم استرداد ${String.format(java.util.Locale.US, "%.2f", refundAmount)} ريال لمحفظتك فوراً!",
                        Toast.LENGTH_LONG
                    ).show()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_submit_dispute_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishRose),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تأكيد إرسال الاعتراض واسترداد المبلغ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
