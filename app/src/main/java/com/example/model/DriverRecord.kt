package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "driver_records")
data class DriverRecord(
    @PrimaryKey
    val driverId: String,
    val fullName: String,
    val phone: String,
    val carPlate: String,
    val carModel: String,
    val carColor: String,
    val rating: Float = 4.9f,
    val totalTripsCount: Int = 0,
    val totalExtraRidesCount: Int = 0,
    val totalWaitingCount: Int = 0,
    val totalEarnings: Double = 0.0,
    val totalPlatformFees: Double = 0.0,
    val walletBalance: Double = 0.0,
    val status: String = "نشط ومتاح",
    val licenseNumber: String = "SA-9948210",
    val joinedDate: String = "2024/01/10"
)
