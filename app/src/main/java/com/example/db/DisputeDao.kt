package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.TripDispute
import kotlinx.coroutines.flow.Flow

@Dao
interface DisputeDao {
    @Query("SELECT * FROM trip_disputes ORDER BY createdAtFormatted DESC")
    fun getAllDisputes(): Flow<List<TripDispute>>

    @Query("SELECT * FROM trip_disputes WHERE tripId = :tripId LIMIT 1")
    suspend fun getDisputeByTripId(tripId: String): TripDispute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: TripDispute)

    @Update
    suspend fun updateDispute(dispute: TripDispute)
}
