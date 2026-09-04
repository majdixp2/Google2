package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MeterMode(val titleAr: String) {
    EXTRA_RIDE("مشوار إضافي"),
    PAID_WAITING("انتظار مدفوع")
}

enum class TripStatus(val titleAr: String) {
    IDLE("جاهز"),
    PENDING_APPROVAL("في انتظار موافقة الراكب"),
    RUNNING("جاري الحساب"),
    PAUSED("متوقف مؤقتاً"),
    STOPPED("تم إيقاف العداد"),
    COMPLETED("مكتملة ومحفوظة")
}

@Entity(tableName = "trip_records")
data class TripRecord(
    @PrimaryKey
    val tripId: String,
    val mode: MeterMode,
    val status: TripStatus,
    val driverId: String = "DRV-101",
    val driverName: String = "أحمد القحطاني",
    val driverPlate: String = "أ ب ج 1 2 3 4",
    val driverCar: String = "تويوتا كامري 2024",
    val passengerId: String = "PSG-501",
    val passengerName: String = "محمد العتيبي",
    val startLocationName: String = "الرياض - طريق الملك فهد",
    val endLocationName: String = "الرياض - واجهة روشن",
    val startLat: Double = 24.7136,
    val startLng: Double = 46.6753,
    val endLat: Double = 24.8465,
    val endLng: Double = 46.7329,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val endTimeMillis: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val distanceMeters: Double = 0.0,
    val idealDistanceMeters: Double = 0.0, // المسافة المثالية لخرائط جوجل
    val idealDurationSeconds: Long = 0,   // الوقت المثالي لخرائط جوجل
    val baseFare: Double = 5.0,
    val distanceCost: Double = 0.0,
    val timeCost: Double = 0.0,
    val taxAmount: Double = 0.0,
    val platformFeeAmount: Double = 0.0,  // رسوم المنصة
    val driverNetPayout: Double = 5.0,   // صافي مستحقات السائق
    val totalFare: Double = 5.0,
    val currency: String = "ريال",
    val isDisputed: Boolean = false,     // هل تم تقديم اعتراض
    val disputeReason: String = "",
    val disputeStatus: String = "NONE",  // NONE, PENDING, RESOLVED, REFUNDED
    val excessDistanceMeters: Double = 0.0,
    val suggestedRefundAmount: Double = 0.0,
    val createdAtFormatted: String = ""
)
