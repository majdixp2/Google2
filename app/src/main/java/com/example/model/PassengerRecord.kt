package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passenger_records")
data class PassengerRecord(
    @PrimaryKey
    val passengerId: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val rating: Float = 5.0f,
    val totalTripsCount: Int = 0,
    val totalExtraRidesCount: Int = 0,
    val totalWaitingCount: Int = 0,
    val totalSpent: Double = 0.0,
    val walletBalance: Double = 250.0,
    val disputesCount: Int = 0,
    val isVerified: Boolean = true,
    val registeredAt: String = "2024/02/01"
)
