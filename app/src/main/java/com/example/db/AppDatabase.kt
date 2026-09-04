package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.model.DriverRecord
import com.example.model.MeterMode
import com.example.model.PassengerRecord
import com.example.model.TariffConfig
import com.example.model.TripDispute
import com.example.model.TripRecord
import com.example.model.TripStatus

class Converters {
    @TypeConverter
    fun fromMeterMode(mode: MeterMode): String = mode.name

    @TypeConverter
    fun toMeterMode(value: String): MeterMode = try {
        MeterMode.valueOf(value)
    } catch (e: Exception) {
        MeterMode.EXTRA_RIDE
    }

    @TypeConverter
    fun fromTripStatus(status: TripStatus): String = status.name

    @TypeConverter
    fun toTripStatus(value: String): TripStatus = try {
        TripStatus.valueOf(value)
    } catch (e: Exception) {
        TripStatus.IDLE
    }
}

@Database(
    entities = [
        TariffConfig::class,
        TripRecord::class,
        DriverRecord::class,
        PassengerRecord::class,
        TripDispute::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tariffDao(): TariffDao
    abstract fun tripDao(): TripDao
    abstract fun driverDao(): DriverDao
    abstract fun passengerDao(): PassengerDao
    abstract fun disputeDao(): DisputeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_meter_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
