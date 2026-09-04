package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.DriverRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {
    @Query("SELECT * FROM driver_records ORDER BY totalTripsCount DESC")
    fun getAllDrivers(): Flow<List<DriverRecord>>

    @Query("SELECT * FROM driver_records WHERE driverId = :driverId LIMIT 1")
    suspend fun getDriverById(driverId: String): DriverRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverRecord>)

    @Update
    suspend fun updateDriver(driver: DriverRecord)
}
