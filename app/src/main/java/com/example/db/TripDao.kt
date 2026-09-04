package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.MeterMode
import com.example.model.TripRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_records ORDER BY startTimeMillis DESC")
    fun getAllTrips(): Flow<List<TripRecord>>

    @Query("SELECT * FROM trip_records WHERE driverId = :driverId ORDER BY startTimeMillis DESC")
    fun getTripsByDriver(driverId: String): Flow<List<TripRecord>>

    @Query("SELECT * FROM trip_records WHERE passengerId = :passengerId ORDER BY startTimeMillis DESC")
    fun getTripsByPassenger(passengerId: String): Flow<List<TripRecord>>

    @Query("SELECT * FROM trip_records WHERE mode = :mode ORDER BY startTimeMillis DESC")
    fun getTripsByMode(mode: MeterMode): Flow<List<TripRecord>>

    @Query("SELECT * FROM trip_records WHERE isDisputed = 1 ORDER BY startTimeMillis DESC")
    fun getDisputedTrips(): Flow<List<TripRecord>>

    @Query("SELECT * FROM trip_records WHERE tripId = :tripId LIMIT 1")
    fun getTripById(tripId: String): Flow<TripRecord?>

    @Query("SELECT * FROM trip_records WHERE tripId = :tripId LIMIT 1")
    suspend fun getTripByIdDirect(tripId: String): TripRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripRecord>)

    @Update
    suspend fun updateTrip(trip: TripRecord)

    @Query("DELETE FROM trip_records WHERE tripId = :tripId")
    suspend fun deleteTrip(tripId: String)
}
