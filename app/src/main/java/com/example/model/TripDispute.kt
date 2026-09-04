package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_disputes")
data class TripDispute(
    @PrimaryKey
    val disputeId: String,
    val tripId: String,
    val passengerId: String = "PSG-501",
    val passengerName: String,
    val driverId: String = "DRV-101",
    val driverName: String,
    val startLocationName: String,
    val endLocationName: String,
    val recordedDistanceMeters: Double,
    val idealDistanceMeters: Double,
    val excessDistanceMeters: Double,
    val excessPercentage: Int,
    val originalFare: Double,
    val correctedFare: Double,
    val refundAmount: Double,
    val reasonCategory: String, // خطأ في الطريق / زيادة غير مبررة / توقف غير مبرر / خلل في العداد
    val passengerNotes: String,
    val googleMapsUrl: String,
    val status: String = "قيد المراجعة والتدقيق", // قيد المراجعة والتدقيق، تم استرداد الفارق للمحفظة، تم رفض الاعتراض
    val createdAtFormatted: String
)
