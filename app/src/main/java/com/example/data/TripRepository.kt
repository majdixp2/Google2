package com.example.data

import com.example.db.DisputeDao
import com.example.db.DriverDao
import com.example.db.PassengerDao
import com.example.db.TariffDao
import com.example.db.TripDao
import com.example.model.DriverRecord
import com.example.model.MeterMode
import com.example.model.PassengerRecord
import com.example.model.TariffConfig
import com.example.model.TripDispute
import com.example.model.TripRecord
import com.example.model.TripStatus
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripRepository(
    private val tariffDao: TariffDao,
    private val tripDao: TripDao,
    private val driverDao: DriverDao,
    private val passengerDao: PassengerDao,
    private val disputeDao: DisputeDao
) {
    val tariffConfig: Flow<TariffConfig?> = tariffDao.getTariffConfig()
    val allTrips: Flow<List<TripRecord>> = tripDao.getAllTrips()
    val allDrivers: Flow<List<DriverRecord>> = driverDao.getAllDrivers()
    val allPassengers: Flow<List<PassengerRecord>> = passengerDao.getAllPassengers()
    val allDisputes: Flow<List<TripDispute>> = disputeDao.getAllDisputes()

    suspend fun initDefaultDataIfNeeded() {
        val existingTariff = tariffDao.getTariffConfigDirect()
        if (existingTariff == null) {
            tariffDao.insertOrUpdateTariff(
                TariffConfig(
                    id = 1,
                    currency = "ريال",
                    extraRideBaseFare = 5.00,
                    extraRidePerKm = 2.00,
                    extraRidePerMin = 0.50,
                    waitingBaseFare = 3.00,
                    waitingPerMin = 1.00,
                    waitingPerKm = 0.00,
                    taxPercentage = 15.0,
                    platformFeeEnabled = false,
                    platformFeePercentage = 10.0,
                    platformFixedFee = 0.0,
                    forceUpdateEnabled = false,
                    minAppVersionCode = 1,
                    latestVersionName = "2.5.0",
                    forceUpdateTitle = "تحديث إلزامي ومطلوب للخدمة",
                    forceUpdateMessage = "يتوجب عليك تحديث تطبيق عداد المشاوير الآن لضمان دقة التسعير والتوافق مع مسارات وتوجيهات الهيئة العامة للنقل.",
                    updateDownloadUrl = "https://play.google.com/store/apps"
                )
            )
        }

        // Initialize sample Drivers with rich realistic fleet data
        val sampleDrivers = listOf(
            DriverRecord(
                driverId = "DRV-101",
                fullName = "أحمد المنصور",
                phone = "+966 55 111 2233",
                carPlate = "ر ح ل ٩ ٨ ٧",
                carModel = "تويوتا كامري 2024",
                carColor = "أسود ملكي",
                rating = 4.95f,
                totalTripsCount = 28,
                totalExtraRidesCount = 21,
                totalWaitingCount = 7,
                totalEarnings = 1420.50,
                totalPlatformFees = 142.05,
                walletBalance = 1278.45,
                status = "نشط ومتاح",
                licenseNumber = "SA-9948210",
                joinedDate = "2024/01/10"
            ),
            DriverRecord(
                driverId = "DRV-102",
                fullName = "فهد الحربي",
                phone = "+966 50 333 4455",
                carPlate = "ط ر ق ٥ ٤ ٣",
                carModel = "هيونداي سوناتا 2023",
                carColor = "فضي ميتاليك",
                rating = 4.88f,
                totalTripsCount = 19,
                totalExtraRidesCount = 13,
                totalWaitingCount = 6,
                totalEarnings = 895.00,
                totalPlatformFees = 89.50,
                walletBalance = 805.50,
                status = "في مشوار حالياً",
                licenseNumber = "SA-8839201",
                joinedDate = "2024/02/15"
            ),
            DriverRecord(
                driverId = "DRV-103",
                fullName = "خالد العتيبي",
                phone = "+966 54 888 9900",
                carPlate = "س ع د ١ ٢ ٣",
                carModel = "نيسان التيما 2024",
                carColor = "أبيض لؤلؤي",
                rating = 4.92f,
                totalTripsCount = 14,
                totalExtraRidesCount = 10,
                totalWaitingCount = 4,
                totalEarnings = 640.00,
                totalPlatformFees = 64.00,
                walletBalance = 576.00,
                status = "نشط ومتاح",
                licenseNumber = "SA-7728190",
                joinedDate = "2024/03/01"
            )
        )
        driverDao.insertDrivers(sampleDrivers)

        // Initialize sample Passengers
        val samplePassengers = listOf(
            PassengerRecord(
                passengerId = "PSG-501",
                fullName = "محمد السعدي",
                phone = "+966 50 123 4567",
                email = "mohammed.saadi@example.com",
                rating = 4.90f,
                totalTripsCount = 34,
                totalExtraRidesCount = 26,
                totalWaitingCount = 8,
                totalSpent = 1180.00,
                walletBalance = 250.00,
                disputesCount = 1,
                isVerified = true,
                registeredAt = "2024/01/15"
            ),
            PassengerRecord(
                passengerId = "PSG-502",
                fullName = "سارة الغامدي",
                phone = "+966 56 777 8899",
                email = "sara.ghamdi@example.com",
                rating = 5.00f,
                totalTripsCount = 16,
                totalExtraRidesCount = 12,
                totalWaitingCount = 4,
                totalSpent = 620.00,
                walletBalance = 180.00,
                disputesCount = 0,
                isVerified = true,
                registeredAt = "2024/02/10"
            ),
            PassengerRecord(
                passengerId = "PSG-503",
                fullName = "عبدالله الشهري",
                phone = "+966 53 444 5566",
                email = "abdullah.shehri@example.com",
                rating = 4.85f,
                totalTripsCount = 9,
                totalExtraRidesCount = 7,
                totalWaitingCount = 2,
                totalSpent = 310.50,
                walletBalance = 95.00,
                disputesCount = 0,
                isVerified = true,
                registeredAt = "2024/03/05"
            )
        )
        passengerDao.insertPassengers(samplePassengers)

        // Add pre-loaded completed sample trips if database is empty so Admin & Passenger history show immediately
        val initialTrip = tripDao.getTripByIdDirect("TRIP-7821")
        if (initialTrip == null) {
            val sampleTrips = listOf(
                TripRecord(
                    tripId = "TRIP-7821",
                    mode = MeterMode.EXTRA_RIDE,
                    status = TripStatus.COMPLETED,
                    driverId = "DRV-101",
                    driverName = "أحمد المنصور",
                    driverPlate = "ر ح ل ٩ ٨ ٧",
                    driverCar = "كامري ٢٠٢٤ - أسود",
                    passengerId = "PSG-501",
                    passengerName = "محمد السعدي",
                    startLocationName = "الرياض - برج المملكة",
                    endLocationName = "الرياض - واجهة روشن",
                    startLat = 24.7115,
                    startLng = 46.6744,
                    endLat = 24.8465,
                    endLng = 46.7329,
                    startTimeMillis = System.currentTimeMillis() - 7200000,
                    endTimeMillis = System.currentTimeMillis() - 5400000,
                    durationSeconds = 1800, // 30 mins
                    distanceMeters = 18400.0, // 18.4 km
                    idealDistanceMeters = 14200.0, // 14.2 km (Direct Maps route)
                    idealDurationSeconds = 1380, // 23 mins
                    baseFare = 5.0,
                    distanceCost = 36.80,
                    timeCost = 15.00,
                    taxAmount = 8.52,
                    platformFeeAmount = 5.68,
                    driverNetPayout = 59.64,
                    totalFare = 65.32,
                    currency = "ريال",
                    isDisputed = false,
                    excessDistanceMeters = 4200.0,
                    suggestedRefundAmount = 8.40,
                    createdAtFormatted = "اليوم - ٠٩:٣٠ ص"
                ),
                TripRecord(
                    tripId = "TRIP-6542",
                    mode = MeterMode.PAID_WAITING,
                    status = TripStatus.COMPLETED,
                    driverId = "DRV-101",
                    driverName = "أحمد المنصور",
                    driverPlate = "ر ح ل ٩ ٨ ٧",
                    driverCar = "كامري ٢٠٢٤ - أسود",
                    passengerId = "PSG-501",
                    passengerName = "محمد السعدي",
                    startLocationName = "الرياض - مجمع الدوائر الحكومية",
                    endLocationName = "الرياض - مجمع الدوائر الحكومية",
                    startLat = 24.6408,
                    startLng = 46.7058,
                    endLat = 24.6408,
                    endLng = 46.7058,
                    startTimeMillis = System.currentTimeMillis() - 18000000,
                    endTimeMillis = System.currentTimeMillis() - 16500000,
                    durationSeconds = 1500, // 25 mins waiting
                    distanceMeters = 0.0,
                    idealDistanceMeters = 0.0,
                    idealDurationSeconds = 1500,
                    baseFare = 3.0,
                    distanceCost = 0.0,
                    timeCost = 25.00,
                    taxAmount = 4.20,
                    platformFeeAmount = 2.80,
                    driverNetPayout = 29.40,
                    totalFare = 32.20,
                    currency = "ريال",
                    isDisputed = false,
                    excessDistanceMeters = 0.0,
                    suggestedRefundAmount = 0.0,
                    createdAtFormatted = "أمس - ٠٤:١٥ م"
                ),
                TripRecord(
                    tripId = "TRIP-5119",
                    mode = MeterMode.EXTRA_RIDE,
                    status = TripStatus.COMPLETED,
                    driverId = "DRV-102",
                    driverName = "فهد الحربي",
                    driverPlate = "ط ر ق ٥ ٤ ٣",
                    driverCar = "هيونداي سوناتا 2023",
                    passengerId = "PSG-502",
                    passengerName = "سارة الغامدي",
                    startLocationName = "الرياض - مطار الملك خالد صالة 5",
                    endLocationName = "الرياض - حي الملقا",
                    startLat = 24.9576,
                    startLng = 46.6988,
                    endLat = 24.7932,
                    endLng = 46.6110,
                    startTimeMillis = System.currentTimeMillis() - 86400000,
                    endTimeMillis = System.currentTimeMillis() - 84000000,
                    durationSeconds = 2400,
                    distanceMeters = 29500.0,
                    idealDistanceMeters = 28200.0,
                    idealDurationSeconds = 2200,
                    baseFare = 5.0,
                    distanceCost = 59.00,
                    timeCost = 20.00,
                    taxAmount = 12.60,
                    platformFeeAmount = 8.40,
                    driverNetPayout = 88.20,
                    totalFare = 96.60,
                    currency = "ريال",
                    isDisputed = false,
                    excessDistanceMeters = 1300.0,
                    suggestedRefundAmount = 2.60,
                    createdAtFormatted = "قبل يومين - ٠٨:١٠ م"
                )
            )
            tripDao.insertTrips(sampleTrips)
        }
    }

    suspend fun getTariffDirect(): TariffConfig {
        return tariffDao.getTariffConfigDirect() ?: TariffConfig()
    }

    suspend fun updateTariff(config: TariffConfig) {
        tariffDao.insertOrUpdateTariff(config)
    }

    suspend fun saveTrip(trip: TripRecord) {
        tripDao.insertTrip(trip)
    }

    suspend fun getTripById(tripId: String): TripRecord? {
        return tripDao.getTripByIdDirect(tripId)
    }

    fun observeTrip(tripId: String): Flow<TripRecord?> {
        return tripDao.getTripById(tripId)
    }

    fun getTripsByDriver(driverId: String): Flow<List<TripRecord>> {
        return tripDao.getTripsByDriver(driverId)
    }

    fun getTripsByPassenger(passengerId: String): Flow<List<TripRecord>> {
        return tripDao.getTripsByPassenger(passengerId)
    }

    suspend fun submitDispute(
        trip: TripRecord,
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
    ): TripDispute {
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date(now))
        val disputeId = "DSP-${(1000..9999).random()}"

        val dispute = TripDispute(
            disputeId = disputeId,
            tripId = trip.tripId,
            passengerId = trip.passengerId,
            passengerName = trip.passengerName,
            driverId = trip.driverId,
            driverName = trip.driverName,
            startLocationName = trip.startLocationName,
            endLocationName = trip.endLocationName,
            recordedDistanceMeters = recordedDistanceKm * 1000.0,
            idealDistanceMeters = idealDistanceKm * 1000.0,
            excessDistanceMeters = excessDistanceKm * 1000.0,
            excessPercentage = excessPercentage,
            originalFare = originalFare,
            correctedFare = correctedFare,
            refundAmount = refundAmount,
            reasonCategory = reasonCategory,
            passengerNotes = passengerNotes,
            googleMapsUrl = googleMapsUrl,
            status = "تم استرداد الفارق للمحفظة فوراً",
            createdAtFormatted = dateStr
        )

        disputeDao.insertDispute(dispute)

        // Mark trip as disputed
        val updatedTrip = trip.copy(
            isDisputed = true,
            disputeReason = reasonCategory,
            disputeStatus = "REFUNDED",
            excessDistanceMeters = excessDistanceKm * 1000.0,
            suggestedRefundAmount = refundAmount
        )
        tripDao.updateTrip(updatedTrip)

        // Refund passenger wallet
        val passenger = passengerDao.getPassengerById(trip.passengerId)
        if (passenger != null) {
            val updatedPassenger = passenger.copy(
                walletBalance = passenger.walletBalance + refundAmount,
                disputesCount = passenger.disputesCount + 1
            )
            passengerDao.updatePassenger(updatedPassenger)
        }

        return dispute
    }
}
