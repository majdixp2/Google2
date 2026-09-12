package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.example.data.TripRepository
import com.example.model.MeterMode
import com.example.model.TariffConfig
import com.example.model.TripRecord
import com.example.model.TripStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

data class LiveMeterState(
    val currentTripId: String = "",
    val mode: MeterMode = MeterMode.EXTRA_RIDE,
    val status: TripStatus = TripStatus.IDLE,
    val isCounting: Boolean = false,
    val durationSeconds: Long = 0,
    val distanceMeters: Double = 0.0,
    val idealDistanceMeters: Double = 0.0,
    val idealDurationSeconds: Long = 0,
    val baseFare: Double = 5.0,
    val distanceCost: Double = 0.0,
    val timeCost: Double = 0.0,
    val taxAmount: Double = 0.0,
    val platformFeeAmount: Double = 0.0,
    val driverNetPayout: Double = 5.0,
    val totalFare: Double = 5.0,
    val speedKmh: Double = 0.0,
    val passengerAccepted: Boolean = false,
    val qrPayload: String = "",
    val driverId: String = "DRV-101",
    val driverName: String = "كابتن أحمد المنصور",
    val driverPlate: String = "ر ح ل ٩ ٨ ٧",
    val driverCar: String = "كامري ٢٠٢٤ - أسود",
    val passengerId: String = "PSG-501",
    val passengerName: String = "محمد السعدي",
    val startLocationName: String = "الرياض - تقاطع التحلية مع العليا",
    val endLocationName: String = "الرياض - واجهة روشن للأعمال",
    val startLat: Double = 24.7001,
    val startLng: Double = 46.6853,
    val endLat: Double = 24.8465,
    val endLng: Double = 46.7329,
    val currentTariff: TariffConfig = TariffConfig(),
    val stoppedStateBackup: StoppedBackup? = null
)

data class StoppedBackup(
    val durationSeconds: Long,
    val distanceMeters: Double,
    val baseFare: Double,
    val distanceCost: Double,
    val timeCost: Double,
    val totalFare: Double
)

class RideMeterManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null
    private var lastLocation: Location? = null
    private var locationManager: LocationManager? = null
    private var isLocationListenerActive = false

    private val _liveState = MutableStateFlow(LiveMeterState())
    val liveState: StateFlow<LiveMeterState> = _liveState.asStateFlow()

    private var repository: TripRepository? = null

    companion object {
        @Volatile
        private var INSTANCE: RideMeterManager? = null

        fun getInstance(context: Context): RideMeterManager {
            return INSTANCE ?: synchronized(this) {
                val instance = RideMeterManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun setRepository(repo: TripRepository) {
        this.repository = repo
        scope.launch {
            repo.tariffConfig.collect { config ->
                if (config != null) {
                    _liveState.update { current ->
                        val base = if (current.mode == MeterMode.EXTRA_RIDE) {
                            config.extraRideBaseFare
                        } else {
                            config.waitingBaseFare
                        }
                        val newBase = if (current.status == TripStatus.IDLE) base else current.baseFare
                        val existingIp = if (current.qrPayload.contains("|IP:")) {
                            current.qrPayload.substringAfter("|IP:").substringBefore("|")
                        } else ""
                        val updated = current.copy(
                            currentTariff = config,
                            baseFare = newBase
                        )
                        val recalculated = recalculate(updated)
                        // Keep the QR payload's embedded tariff in sync with the real config —
                        // otherwise a code generated before the config finished loading would
                        // keep advertising stale default rates to anyone who scans it.
                        if (current.status == TripStatus.IDLE) {
                            recalculated.copy(qrPayload = buildPayload(recalculated, existingIp))
                        } else {
                            recalculated
                        }
                    }
                }
            }
        }
    }

    init {
        initNewTripId(MeterMode.EXTRA_RIDE)
        startLocationUpdates()
    }

    /**
     * Builds the RIDE_METER QR/trip-code payload carrying the FULL real tariff
     * (base + per-km + per-min + tax), not just the base fare — otherwise a
     * scanning passenger device would only learn the base fare and keep showing
     * its own local default rates for everything else.
     */
    private fun buildPayload(state: LiveMeterState, hostIp: String = ""): String {
        val tariff = state.currentTariff
        val perKm = if (state.mode == MeterMode.EXTRA_RIDE) tariff.extraRidePerKm else tariff.waitingPerKm
        val perMin = if (state.mode == MeterMode.EXTRA_RIDE) tariff.extraRidePerMin else tariff.waitingPerMin
        val ipPart = if (hostIp.isNotBlank()) "|IP:$hostIp" else ""
        return "RIDE_METER|ID:${state.currentTripId}|MODE:${state.mode.name}|BASE:${state.baseFare}" +
            "|PERKM:$perKm|PERMIN:$perMin|TAX:${tariff.taxPercentage}|CUR:${tariff.currency}$ipPart" +
            "|DATE:${System.currentTimeMillis()}"
    }

    fun initNewTripId(mode: MeterMode) {
        val randomNum = 1000 + Random().nextInt(9000)
        val newTripId = "TRIP-$randomNum"
        val tariff = _liveState.value.currentTariff
        val base = if (mode == MeterMode.EXTRA_RIDE) tariff.extraRideBaseFare else tariff.waitingBaseFare

        _liveState.update {
            val withBase = it.copy(
                currentTripId = newTripId,
                mode = mode,
                status = TripStatus.IDLE,
                isCounting = false,
                durationSeconds = 0,
                distanceMeters = 0.0,
                idealDistanceMeters = 0.0,
                idealDurationSeconds = 0,
                baseFare = base,
                distanceCost = 0.0,
                timeCost = 0.0,
                taxAmount = 0.0,
                platformFeeAmount = 0.0,
                driverNetPayout = base,
                totalFare = base,
                passengerAccepted = false,
                stoppedStateBackup = null
            )
            withBase.copy(qrPayload = buildPayload(withBase))
        }
    }

    fun selectMode(mode: MeterMode) {
        if (_liveState.value.isCounting) return
        val tariff = _liveState.value.currentTariff
        val base = if (mode == MeterMode.EXTRA_RIDE) tariff.extraRideBaseFare else tariff.waitingBaseFare
        _liveState.update {
            val updated = it.copy(
                mode = mode,
                baseFare = base
            )
            val recalculated = recalculate(updated)
            recalculated.copy(qrPayload = buildPayload(recalculated))
        }
    }

    fun shareBarcode(hostIp: String = "") {
        _liveState.update { current ->
            current.copy(
                status = if (current.status == TripStatus.IDLE) TripStatus.PENDING_APPROVAL else current.status,
                qrPayload = buildPayload(current, hostIp)
            )
        }
    }

    fun updateExternalLiveState(newState: LiveMeterState) {
        _liveState.update { current ->
            newState.copy(
                // Keep tariff and backup intact if external is default
                currentTariff = if (newState.currentTariff.extraRidePerKm > 0) newState.currentTariff else current.currentTariff
            )
        }
    }

    fun applyTripPayload(payload: String): Boolean {
        try {
            if (!payload.contains("RIDE_METER|")) {
                // If it's just a raw trip id like TRIP-1234
                if (payload.isNotBlank()) {
                    _liveState.update { it.copy(currentTripId = payload.trim().uppercase()) }
                    return true
                }
                return false
            }
            val parts = payload.split("|")
            var tripId = ""
            var modeName = ""
            var baseFareVal = 5.0
            var perKmVal: Double? = null
            var perMinVal: Double? = null
            var taxVal: Double? = null
            var currencyVal: String? = null
            var hostIp = ""

            for (part in parts) {
                when {
                    part.startsWith("ID:") -> tripId = part.substring(3).trim()
                    part.startsWith("MODE:") -> modeName = part.substring(5).trim()
                    part.startsWith("BASE:") -> baseFareVal = part.substring(5).trim().toDoubleOrNull() ?: 5.0
                    part.startsWith("PERKM:") -> perKmVal = part.substring(6).trim().toDoubleOrNull()
                    part.startsWith("PERMIN:") -> perMinVal = part.substring(7).trim().toDoubleOrNull()
                    part.startsWith("TAX:") -> taxVal = part.substring(4).trim().toDoubleOrNull()
                    part.startsWith("CUR:") -> currencyVal = part.substring(4).trim()
                    part.startsWith("IP:") -> hostIp = part.substring(3).trim()
                }
            }

            val mode = try { MeterMode.valueOf(modeName) } catch (e: Exception) { MeterMode.EXTRA_RIDE }
            if (tripId.isNotBlank()) {
                _liveState.update { current ->
                    // Build the REAL scanned tariff instead of relying on this device's
                    // own local (possibly never-configured) default TariffConfig.
                    val scannedTariff = if (perKmVal != null || perMinVal != null || taxVal != null) {
                        val base = current.currentTariff
                        if (mode == MeterMode.EXTRA_RIDE) {
                            base.copy(
                                extraRideBaseFare = baseFareVal,
                                extraRidePerKm = perKmVal ?: base.extraRidePerKm,
                                extraRidePerMin = perMinVal ?: base.extraRidePerMin,
                                taxPercentage = taxVal ?: base.taxPercentage,
                                currency = currencyVal ?: base.currency
                            )
                        } else {
                            base.copy(
                                waitingBaseFare = baseFareVal,
                                waitingPerMin = perMinVal ?: base.waitingPerMin,
                                waitingPerKm = perKmVal ?: base.waitingPerKm,
                                taxPercentage = taxVal ?: base.taxPercentage,
                                currency = currencyVal ?: base.currency
                            )
                        }
                    } else {
                        current.currentTariff
                    }
                    val updated = current.copy(
                        currentTripId = tripId,
                        mode = mode,
                        baseFare = baseFareVal,
                        currentTariff = scannedTariff,
                        qrPayload = payload
                    )
                    recalculate(updated)
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Called on the PASSENGER's device when they tap "accept fare". This only
     * marks local UI flags — it must NEVER start a local ticker (startCounting()),
     * because the passenger's live numbers must come exclusively from the driver's
     * synced state via updateExternalLiveState(). Running a local ticker here would
     * make the passenger's screen compute its own independent (and wrong) fare in
     * parallel with the real synced value from the driver.
     */
    fun passengerAcceptsFare(tripId: String): Boolean {
        val cleanTripId = tripId.trim().uppercase()
        _liveState.update {
            it.copy(
                currentTripId = if (cleanTripId.isNotBlank()) cleanTripId else it.currentTripId,
                passengerAccepted = true,
                status = TripStatus.RUNNING
            )
        }
        return true
    }

    fun startCounting() {
        if (tickerJob?.isActive == true) return
        _liveState.update {
            it.copy(
                isCounting = true,
                status = TripStatus.RUNNING
            )
        }
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000)
                _liveState.update { state ->
                    if (!state.isCounting) return@update state
                    val newDuration = state.durationSeconds + 1
                    // Distance is no longer simulated here — it is driven exclusively by
                    // real GPS fixes in setupLocationListener(). This tick only advances
                    // elapsed time (and therefore the waiting/time-based fare component),
                    // and lets the displayed speed decay toward zero if no fresh GPS fix
                    // has arrived recently (e.g. the vehicle is stopped or signal is weak).
                    val decayedSpeed = if (state.mode == MeterMode.EXTRA_RIDE) {
                        state.speedKmh * 0.6
                    } else {
                        0.0
                    }
                    val updated = state.copy(
                        durationSeconds = newDuration,
                        speedKmh = decayedSpeed
                    )
                    recalculate(updated)
                }
            }
        }
    }

    fun pauseCounting() {
        tickerJob?.cancel()
        _liveState.update {
            it.copy(
                isCounting = false,
                status = TripStatus.PAUSED,
                speedKmh = 0.0
            )
        }
    }

    fun stopTrip() {
        tickerJob?.cancel()
        val current = _liveState.value
        val backup = StoppedBackup(
            durationSeconds = current.durationSeconds,
            distanceMeters = current.distanceMeters,
            baseFare = current.baseFare,
            distanceCost = current.distanceCost,
            timeCost = current.timeCost,
            totalFare = current.totalFare
        )
        _liveState.update {
            it.copy(
                isCounting = false,
                status = TripStatus.STOPPED,
                speedKmh = 0.0,
                stoppedStateBackup = backup
            )
        }
    }

    fun resumeTripIfStoppedByMistake() {
        _liveState.update {
            it.copy(
                isCounting = true,
                status = TripStatus.RUNNING
            )
        }
        startCounting()
    }

    fun finishAndSaveTrip() {
        val state = _liveState.value
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date(now))
        
        // Compute ideal direct route baseline for Google Maps verification
        val idealDist = if (state.mode == MeterMode.EXTRA_RIDE && state.distanceMeters > 500) {
            state.distanceMeters * 0.82 // Direct route is usually 15-20% shorter if optimal
        } else {
            state.distanceMeters
        }
        val idealDuration = if (state.mode == MeterMode.EXTRA_RIDE && state.durationSeconds > 60) {
            (state.durationSeconds * 0.85).toLong()
        } else {
            state.durationSeconds
        }
        val excessDist = if (state.distanceMeters > idealDist) (state.distanceMeters - idealDist) else 0.0
        val suggestedRefund = if (excessDist > 500 && state.mode == MeterMode.EXTRA_RIDE) {
            (excessDist / 1000.0) * state.currentTariff.extraRidePerKm
        } else {
            0.0
        }

        val record = TripRecord(
            tripId = state.currentTripId,
            mode = state.mode,
            status = TripStatus.COMPLETED,
            driverId = state.driverId,
            driverName = state.driverName,
            driverPlate = state.driverPlate,
            driverCar = state.driverCar,
            passengerId = state.passengerId,
            passengerName = state.passengerName,
            startLocationName = state.startLocationName,
            endLocationName = state.endLocationName,
            startLat = state.startLat,
            startLng = state.startLng,
            endLat = state.endLat,
            endLng = state.endLng,
            startTimeMillis = now - (state.durationSeconds * 1000),
            endTimeMillis = now,
            durationSeconds = state.durationSeconds,
            distanceMeters = state.distanceMeters,
            idealDistanceMeters = idealDist,
            idealDurationSeconds = idealDuration,
            baseFare = state.baseFare,
            distanceCost = state.distanceCost,
            timeCost = state.timeCost,
            taxAmount = state.taxAmount,
            platformFeeAmount = state.platformFeeAmount,
            driverNetPayout = state.driverNetPayout,
            totalFare = state.totalFare,
            currency = state.currentTariff.currency,
            isDisputed = false,
            excessDistanceMeters = excessDist,
            suggestedRefundAmount = suggestedRefund,
            createdAtFormatted = dateStr
        )
        scope.launch {
            repository?.saveTrip(record)
        }
        _liveState.update {
            it.copy(status = TripStatus.COMPLETED)
        }
        // Prepare next trip
        scope.launch {
            delay(1200)
            initNewTripId(state.mode)
        }
    }

    private fun recalculate(state: LiveMeterState): LiveMeterState {
        val tariff = state.currentTariff
        val base = if (state.mode == MeterMode.EXTRA_RIDE) tariff.extraRideBaseFare else tariff.waitingBaseFare
        val distKm = state.distanceMeters / 1000.0
        val distRate = if (state.mode == MeterMode.EXTRA_RIDE) tariff.extraRidePerKm else tariff.waitingPerKm
        val distCost = distKm * distRate

        val minutes = state.durationSeconds / 60.0
        val minRate = if (state.mode == MeterMode.EXTRA_RIDE) tariff.extraRidePerMin else tariff.waitingPerMin
        val timeCost = minutes * minRate

        val subtotal = base + distCost + timeCost
        val tax = subtotal * (tariff.taxPercentage / 100.0)
        val grandTotal = subtotal + tax

        // Platform fee calculation (if enabled)
        val platformFee = if (tariff.platformFeeEnabled) {
            (grandTotal * (tariff.platformFeePercentage / 100.0)) + tariff.platformFixedFee
        } else {
            0.0
        }
        val driverNet = grandTotal - platformFee

        return state.copy(
            baseFare = base,
            distanceCost = distCost,
            timeCost = timeCost,
            taxAmount = tax,
            platformFeeAmount = platformFee,
            driverNetPayout = driverNet,
            totalFare = grandTotal
        )
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (isLocationListenerActive) return
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            var hasGpsFix = false

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    // Once a real GPS fix has arrived, ignore the much less accurate
                    // Network provider entirely — mixing the two causes phantom
                    // "movement" while stationary, since their reported positions for
                    // the same physical spot commonly differ by tens of meters.
                    val isGps = location.provider == LocationManager.GPS_PROVIDER
                    if (isGps) hasGpsFix = true
                    if (!isGps && hasGpsFix) return

                    // Reject fixes with poor accuracy — real GPS is typically 3-10m
                    // outdoors; Network fixes are commonly 20-100m. A high accuracy
                    // radius means the reported position itself is unreliable.
                    if (location.hasAccuracy() && location.accuracy > 25f) {
                        return
                    }

                    if (!_liveState.value.isCounting || _liveState.value.mode != MeterMode.EXTRA_RIDE) {
                        lastLocation = location
                        return
                    }
                    val prev = lastLocation
                    if (prev != null) {
                        val distance = prev.distanceTo(location).toDouble()
                        val elapsedSeconds = (location.time - prev.time) / 1000.0
                        // Reject implausible jumps: a taxi ride won't realistically exceed
                        // ~55 m/s (~200 km/h); anything faster is almost certainly a bad fix.
                        val impliedSpeedMs = if (elapsedSeconds > 0) distance / elapsedSeconds else 0.0
                        if (distance in 1.0..100.0 && impliedSpeedMs <= 55.0) {
                            _liveState.update { state ->
                                val updated = state.copy(
                                    distanceMeters = state.distanceMeters + distance,
                                    speedKmh = (location.speed * 3.6).toDouble()
                                )
                                recalculate(updated)
                            }
                        }
                    }
                    lastLocation = location
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            var registered = false
            if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1500L,
                    2f,
                    listener
                )
                registered = true
            }
            // Network-based provider as a fallback ONLY until a real GPS fix arrives
            // (e.g. brief indoor start-up) — the listener above stops honoring it
            // the moment GPS kicks in, to avoid mixing accuracy levels.
            if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1500L,
                    2f,
                    listener
                )
                registered = true
            }
            isLocationListenerActive = registered
        } catch (e: SecurityException) {
            // Runtime permission not granted yet — the calling screen is expected to
            // request ACCESS_FINE_LOCATION and call startLocationUpdates() again on grant.
            isLocationListenerActive = false
        } catch (e: Exception) {
            isLocationListenerActive = false
        }
    }
}
