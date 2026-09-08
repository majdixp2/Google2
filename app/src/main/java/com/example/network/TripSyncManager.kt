package com.example.network

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import com.example.engine.LiveMeterState
import com.example.engine.RideMeterManager
import com.example.model.MeterMode
import com.example.model.TripStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * Handles Real-Time Synchronisation between Driver and Passenger devices.
 * Supports:
 * 1. Cloud Relay / Realtime API (Internet).
 * 2. Local P2P Server / Wi-Fi / Hotspot without internet.
 */
class TripSyncManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .build()

    // Free, reliable public real-time key-value / pubsub relay for instant syncing
    // Can also fallback to local server
    private val cloudRelayUrl = "https://kvdb.io/NFYnBNshkab7oLvow2PJPK"

    private var localServerSocket: ServerSocket? = null
    private var isServerRunning = false
    private var pollingJob: Job? = null
    private var driverBroadcastJob: Job? = null

    /**
     * The most recent driver IP the passenger successfully connected to
     * (set right after a QR scan / manual trip-code connect). Used so that
     * the "Accept fare" action can reach the driver directly over the local
     * network instead of relying solely on the cloud relay.
     */
    var lastKnownDriverIp: String? = null

    companion object {
        const val DEFAULT_PORT = 8998

        @Volatile
        private var INSTANCE: TripSyncManager? = null

        fun getInstance(context: Context): TripSyncManager {
            return INSTANCE ?: synchronized(this) {
                val inst = TripSyncManager(context.applicationContext)
                INSTANCE = inst
                inst
            }
        }
    }

    /**
     * Get the device IP address on Local Wi-Fi or Hotspot
     */
    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress ?: ""
                        if (host.isNotBlank() && !host.startsWith("127.")) {
                            return host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }

    /**
     * Start Local Micro HTTP Server on the Driver's phone
     */
    fun startDriverServer(meterManager: RideMeterManager) {
        if (isServerRunning) return
        isServerRunning = true

        scope.launch {
            try {
                localServerSocket = ServerSocket(DEFAULT_PORT)
                while (isServerRunning && isActive) {
                    val clientSocket = localServerSocket?.accept() ?: break
                    handleClientRequest(clientSocket, meterManager)
                }
            } catch (e: Exception) {
                // Socket closed or error
            }
        }

        // Also periodically sync state to Cloud Relay so it works over 4G/5G internet as well!
        startDriverCloudBroadcast(meterManager)
    }

    private fun startDriverCloudBroadcast(meterManager: RideMeterManager) {
        driverBroadcastJob?.cancel()
        driverBroadcastJob = scope.launch {
            while (isActive) {
                val state = meterManager.liveState.value
                val cleanTripId = state.currentTripId.trim().uppercase()
                if (cleanTripId.isNotBlank()) {
                    val json = stateToJson(state)
                    sendToCloud(cleanTripId, json)

                    // Also check if passenger clicked Accept via cloud
                    if (!state.passengerAccepted && !state.isCounting) {
                        try {
                            val acceptKey = "ACCEPT_$cleanTripId"
                            val req = Request.Builder().url("$cloudRelayUrl/$acceptKey").get().build()
                            httpClient.newCall(req).execute().use { resp ->
                                if (resp.isSuccessful) {
                                    val body = resp.body?.string()
                                    if (body?.contains("\"accepted\":true") == true) {
                                        withContext(Dispatchers.Main) {
                                            meterManager.passengerAcceptsFare(cleanTripId)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
                delay(1200) // Sync every 1.2 seconds
            }
        }
    }

    private suspend fun sendToCloud(tripId: String, json: String) {
        withContext(Dispatchers.IO) {
            try {
                val key = "RIDE_${tripId.trim().uppercase()}"
                val body = json.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$cloudRelayUrl/$key")
                    .post(body)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (e: Exception) {
                // Ignore silent network errors (offline mode fallback)
            }
        }
    }

    private fun handleClientRequest(socket: Socket, meterManager: RideMeterManager) {
        scope.launch {
            try {
                socket.use { s ->
                    val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                    val writer = PrintWriter(s.getOutputStream(), true)

                    val requestLine = reader.readLine() ?: return@launch
                    val parts = requestLine.split(" ")
                    val method = if (parts.isNotEmpty()) parts[0] else "GET"
                    val path = if (parts.size > 1) parts[1] else "/"

                    when {
                        // GET /trip or /state -> return live meter json
                        method == "GET" && (path.startsWith("/trip") || path.startsWith("/state")) -> {
                            val state = meterManager.liveState.value
                            val json = stateToJson(state)
                            writer.print("HTTP/1.1 200 OK\r\n")
                            writer.print("Content-Type: application/json; charset=utf-8\r\n")
                            writer.print("Access-Control-Allow-Origin: *\r\n")
                            writer.print("Content-Length: ${json.toByteArray().size}\r\n")
                            writer.print("\r\n")
                            writer.print(json)
                            writer.flush()
                        }
                        // POST /accept -> passenger accepted the fare
                        method == "POST" && path.startsWith("/accept") -> {
                            meterManager.startCounting()
                            val response = """{"status":"ACCEPTED","isCounting":true}"""
                            writer.print("HTTP/1.1 200 OK\r\n")
                            writer.print("Content-Type: application/json\r\n")
                            writer.print("Access-Control-Allow-Origin: *\r\n")
                            writer.print("Content-Length: ${response.length}\r\n")
                            writer.print("\r\n")
                            writer.print(response)
                            writer.flush()
                        }
                        else -> {
                            val state = meterManager.liveState.value
                            val json = stateToJson(state)
                            writer.print("HTTP/1.1 200 OK\r\n")
                            writer.print("Content-Type: application/json; charset=utf-8\r\n")
                            writer.print("Access-Control-Allow-Origin: *\r\n")
                            writer.print("Content-Length: ${json.toByteArray().size}\r\n")
                            writer.print("\r\n")
                            writer.print(json)
                            writer.flush()
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore socket read/write failures
            }
        }
    }

    /**
     * Convenience method for starting passenger sync directly binding to RideMeterManager
     */
    fun startPassengerSync(
        meterManager: RideMeterManager,
        tripId: String,
        driverIp: String = ""
    ) {
        startPassengerSync(
            tripId = tripId,
            targetHostIp = driverIp.ifBlank { null },
            onUpdate = { updatedState ->
                meterManager.updateExternalLiveState(updatedState)
            }
        )
    }

    /**
     * Passenger begins continuously fetching the live trip from Driver
     * Checks both Direct Local IP (if on same hotspot) and Cloud Relay (if over mobile internet)
     */
    fun startPassengerSync(
        tripId: String,
        targetHostIp: String? = null,
        onUpdate: (LiveMeterState) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            val cleanTripId = tripId.trim().uppercase()
            while (isActive) {
                var fetched = false

                // 1. Try local Wi-Fi / Hotspot server if IP is available
                if (!targetHostIp.isNullOrBlank()) {
                    try {
                        val localUrl = "http://$targetHostIp:$DEFAULT_PORT/state"
                        val req = Request.Builder().url(localUrl).get().build()
                        httpClient.newCall(req).execute().use { resp ->
                            if (resp.isSuccessful) {
                                val body = resp.body?.string()
                                if (!body.isNullOrBlank()) {
                                    val state = parseJsonToState(body, cleanTripId)
                                    withContext(Dispatchers.Main) { onUpdate(state) }
                                    fetched = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Local fetch failed, fallback to cloud
                    }
                }

                // 2. Try Cloud Relay (works seamlessly across separate 4G/5G mobile data networks!)
                if (!fetched) {
                    try {
                        val key = "RIDE_$cleanTripId"
                        val req = Request.Builder().url("$cloudRelayUrl/$key").get().build()
                        httpClient.newCall(req).execute().use { resp ->
                            if (resp.isSuccessful) {
                                val body = resp.body?.string()
                                if (!body.isNullOrBlank()) {
                                    val state = parseJsonToState(body, cleanTripId)
                                    withContext(Dispatchers.Main) { onUpdate(state) }
                                    fetched = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Cloud sync error
                    }
                }

                delay(1200) // Poll update interval
            }
        }
    }

    /**
     * Send passenger acceptance to Driver (both local and cloud)
     */
    suspend fun sendPassengerAcceptance(tripId: String, targetHostIp: String? = null): Boolean {
        return withContext(Dispatchers.IO) {
            val cleanTripId = tripId.trim().uppercase()
            var ok = false

            // Try local
            if (!targetHostIp.isNullOrBlank()) {
                try {
                    val req = Request.Builder()
                        .url("http://$targetHostIp:$DEFAULT_PORT/accept")
                        .post("{}".toRequestBody("application/json".toMediaType()))
                        .build()
                    httpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) ok = true
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            // Also post accepted state update to Cloud
            try {
                val key = "ACCEPT_$cleanTripId"
                val req = Request.Builder()
                    .url("$cloudRelayUrl/$key")
                    .post("""{"accepted":true,"timestamp":${System.currentTimeMillis()}}""".toRequestBody("application/json".toMediaType()))
                    .build()
                httpClient.newCall(req).execute().close()
                ok = true
            } catch (e: Exception) {
                // Ignore
            }

            ok
        }
    }

    fun stopPassengerSync() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun stopDriverServer() {
        isServerRunning = false
        driverBroadcastJob?.cancel()
        driverBroadcastJob = null
        try {
            localServerSocket?.close()
        } catch (e: Exception) {
            // Ignore
        }
        localServerSocket = null
    }

    private fun stateToJson(state: LiveMeterState): String {
        val obj = JSONObject().apply {
            put("tripId", state.currentTripId)
            put("mode", state.mode.name)
            put("status", state.status.name)
            put("isCounting", state.isCounting)
            put("durationSeconds", state.durationSeconds)
            put("distanceMeters", state.distanceMeters)
            put("baseFare", state.baseFare)
            put("distanceCost", state.distanceCost)
            put("timeCost", state.timeCost)
            put("taxAmount", state.taxAmount)
            put("platformFeeAmount", state.platformFeeAmount)
            put("driverNetPayout", state.driverNetPayout)
            put("totalFare", state.totalFare)
            put("speedKmh", state.speedKmh)
            put("driverName", state.driverName)
            put("driverPlate", state.driverPlate)
            put("driverCar", state.driverCar)
            put("qrPayload", state.qrPayload)
        }
        return obj.toString()
    }

    private fun parseJsonToState(json: String, fallbackTripId: String): LiveMeterState {
        val obj = JSONObject(json)
        val modeStr = obj.optString("mode", "EXTRA_RIDE")
        val mode = try { MeterMode.valueOf(modeStr) } catch (e: Exception) { MeterMode.EXTRA_RIDE }
        val statusStr = obj.optString("status", "RUNNING")
        val status = try { TripStatus.valueOf(statusStr) } catch (e: Exception) { TripStatus.RUNNING }

        return LiveMeterState(
            currentTripId = obj.optString("tripId", fallbackTripId),
            mode = mode,
            status = status,
            isCounting = obj.optBoolean("isCounting", true),
            durationSeconds = obj.optLong("durationSeconds", 0L),
            distanceMeters = obj.optDouble("distanceMeters", 0.0),
            baseFare = obj.optDouble("baseFare", 5.0),
            distanceCost = obj.optDouble("distanceCost", 0.0),
            timeCost = obj.optDouble("timeCost", 0.0),
            taxAmount = obj.optDouble("taxAmount", 0.0),
            platformFeeAmount = obj.optDouble("platformFeeAmount", 0.0),
            driverNetPayout = obj.optDouble("driverNetPayout", 5.0),
            totalFare = obj.optDouble("totalFare", 5.0),
            speedKmh = obj.optDouble("speedKmh", 0.0),
            driverName = obj.optString("driverName", "كابتن أحمد المنصور"),
            driverPlate = obj.optString("driverPlate", "ر ح ل ٩ ٨ ٧"),
            driverCar = obj.optString("driverCar", "كامري ٢٠٢٤ - أسود"),
            qrPayload = obj.optString("qrPayload", "")
        )
    }
}
