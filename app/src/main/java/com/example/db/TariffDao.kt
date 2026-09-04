package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.TariffConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface TariffDao {
    @Query("SELECT * FROM tariff_config WHERE id = 1 LIMIT 1")
    fun getTariffConfig(): Flow<TariffConfig?>

    @Query("SELECT * FROM tariff_config WHERE id = 1 LIMIT 1")
    suspend fun getTariffConfigDirect(): TariffConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTariff(config: TariffConfig)
}
