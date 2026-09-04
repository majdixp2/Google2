package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.PassengerRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PassengerDao {
    @Query("SELECT * FROM passenger_records ORDER BY totalTripsCount DESC")
    fun getAllPassengers(): Flow<List<PassengerRecord>>

    @Query("SELECT * FROM passenger_records WHERE passengerId = :passengerId LIMIT 1")
    suspend fun getPassengerById(passengerId: String): PassengerRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassenger(passenger: PassengerRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassengers(passengers: List<PassengerRecord>)

    @Update
    suspend fun updatePassenger(passenger: PassengerRecord)
}
