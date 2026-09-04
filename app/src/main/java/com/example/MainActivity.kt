package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.TripRepository
import com.example.db.AppDatabase
import com.example.engine.RideMeterManager
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.AppTheme
import com.example.ui.theme.SmartRideMeterTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: TripRepository
    private lateinit var meterManager: RideMeterManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(applicationContext)
        repository = TripRepository(
            tariffDao = database.tariffDao(),
            tripDao = database.tripDao(),
            driverDao = database.driverDao(),
            passengerDao = database.passengerDao(),
            disputeDao = database.disputeDao()
        )
        meterManager = RideMeterManager.getInstance(applicationContext).apply {
            setRepository(repository)
        }

        setContent {
            SmartRideMeterTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = AppTheme.colors.bg
                    ) {
                        AppNavigation(
                            meterManager = meterManager,
                            repository = repository
                        )
                    }
                }
            }
        }
    }
}
