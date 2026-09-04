package com.example.model

data class UserProfile(
    val id: String = "USER_01",
    val fullName: String = "محمد السعدي",
    val email: String = "passenger@example.com",
    val phone: String = "+966 50 123 4567",
    val isDriver: Boolean = false,
    val walletBalance: Double = 250.00,
    val biometricEnabled: Boolean = true,
    val rating: Float = 4.9f,
    val totalTrips: Int = 34
)
