package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tariff_config")
data class TariffConfig(
    @PrimaryKey
    val id: Int = 1,
    val currency: String = "ريال",
    // Extra Ride (مشوار إضافي)
    val extraRideBaseFare: Double = 5.00, // فتح العداد
    val extraRidePerKm: Double = 2.00,    // سعر الكيلومتر
    val extraRidePerMin: Double = 0.50,   // سعر الدقيقة
    // Paid Waiting (انتظار مدفوع)
    val waitingBaseFare: Double = 3.00,   // فتح العداد للانتظار
    val waitingPerMin: Double = 1.00,     // سعر دقيقة الانتظار
    val waitingPerKm: Double = 0.00,      // سعر المسافة أثناء الانتظار
    // Taxes & Platform Fees (الضرائب ورسوم المنصة المستقبلية)
    val taxPercentage: Double = 15.0,     // ضريبة القيمة المضافة %
    val platformFeeEnabled: Boolean = false, // تفعيل فرض رسوم المنصة
    val platformFeePercentage: Double = 10.0, // نسبة عمولة المنصة %
    val platformFixedFee: Double = 0.0,   // رسوم ثابتة لكل مشوار
    // Force Update Controls (التحكم في التحديث الإجباري)
    val forceUpdateEnabled: Boolean = false, // تفعيل التحديث الإجباري لجميع الأجهزة
    val minAppVersionCode: Int = 1,          // أدنى رقم إصدار مسموح به
    val latestVersionName: String = "2.5.0", // رقم النسخة الأحدث
    val forceUpdateTitle: String = "تحديث إلزامي ومطلوب للخدمة",
    val forceUpdateMessage: String = "يتوجب عليك تحديث تطبيق عداد المشاوير الآن لضمان دقة التسعير والتوافق مع مسارات وتوجيهات الهيئة العامة للنقل.",
    val updateDownloadUrl: String = "https://play.google.com/store/apps"
)
