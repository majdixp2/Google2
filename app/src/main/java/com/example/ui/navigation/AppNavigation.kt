package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.TripRepository
import com.example.engine.RideMeterManager
import com.example.model.TariffConfig
import com.example.ui.components.ForceUpdateDialog
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminPricingScreen
import com.example.ui.screens.DriverHomeScreen
import com.example.ui.screens.FareApprovalScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PassengerHomeScreen
import com.example.ui.screens.PassengerLiveTripScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TripHistoryScreen

object Destinations {
    const val LOGIN = "login"
    const val PASSENGER_HOME = "passenger_home"
    const val FARE_APPROVAL = "fare_approval/{tripId}"
    const val PASSENGER_LIVE_TRIP = "passenger_live_trip"
    const val DRIVER_HOME = "driver_home"
    const val ADMIN_PRICING = "admin_pricing"
    const val TRIP_HISTORY = "trip_history"
    const val SUPPORT = "support"
    const val ACCOUNT = "account"

    fun fareApprovalRoute(tripId: String): String = "fare_approval/${tripId.ifBlank { "ACTIVE" }}"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    meterManager: RideMeterManager,
    repository: TripRepository
) {
    val tariffState by repository.tariffConfig.collectAsState(initial = null)
    var adminBypassActive by remember { mutableStateOf(false) }

    val currentTariff = tariffState ?: TariffConfig()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Destinations.LOGIN
        ) {
            // 1. LOGIN SCREEN (الراكب + المقاييس الحيوية + زر السائق + زر المسؤول)
            composable(Destinations.LOGIN) {
                LoginScreen(
                    onLoginAsPassenger = {
                        navController.navigate(Destinations.PASSENGER_HOME)
                    },
                    onNavigateToDriver = {
                        navController.navigate(Destinations.DRIVER_HOME)
                    },
                    onNavigateToAdmin = {
                        navController.navigate(Destinations.ADMIN_PRICING)
                    }
                )
            }

            // 2. PASSENGER MAIN SCREEN (امسح الباركود أو أدخل رقم الرحلة + القائمة الجانبية)
            composable(Destinations.PASSENGER_HOME) {
                PassengerHomeScreen(
                    meterManager = meterManager,
                    onNavigateBackToLogin = {
                        navController.popBackStack(Destinations.LOGIN, inclusive = false)
                    },
                    onNavigateToFareApproval = { tripId ->
                        navController.navigate(Destinations.fareApprovalRoute(tripId))
                    },
                    onNavigateToLiveTrip = {
                        navController.navigate(Destinations.PASSENGER_LIVE_TRIP)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Destinations.TRIP_HISTORY)
                    },
                    onNavigateToSupport = {
                        navController.navigate(Destinations.SUPPORT)
                    },
                    onNavigateToAccount = {
                        navController.navigate(Destinations.ACCOUNT)
                    }
                )
            }

            // 3. FARE APPROVAL SCREEN (الموافقة على التسعيرة قبل بدء العد)
            composable(
                route = Destinations.FARE_APPROVAL,
                arguments = listOf(navArgument("tripId") { type = NavType.StringType; defaultValue = "TRIP-LIVE" })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: "TRIP-LIVE"
                FareApprovalScreen(
                    tripId = tripId,
                    meterManager = meterManager,
                    onAcceptAndStart = {
                        navController.navigate(Destinations.PASSENGER_LIVE_TRIP) {
                            popUpTo(Destinations.PASSENGER_HOME)
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 4. PASSENGER LIVE TRIP SCREEN (العداد المباشر الحية + بوليصة الرحلة + زر اعتراض خرائط قوقل)
            composable(Destinations.PASSENGER_LIVE_TRIP) {
                PassengerLiveTripScreen(
                    meterManager = meterManager,
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 5. DRIVER MAIN SCREEN (الزرين الكبيران: انتظار مدفوع و مشوار اضافي + شارك الباركود + زر تصحيح الخطأ)
            composable(Destinations.DRIVER_HOME) {
                DriverHomeScreen(
                    meterManager = meterManager,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToAdmin = {
                        navController.navigate(Destinations.ADMIN_PRICING)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Destinations.TRIP_HISTORY)
                    }
                )
            }

            // 6. ADMIN PRICING & MANAGEMENT DASHBOARD (التسعيرات والرسوم + سجلات السائقين + سجلات الركاب + التحديث الإجباري)
            composable(Destinations.ADMIN_PRICING) {
                AdminPricingScreen(
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 7. TRIP HISTORY SCREEN (سجل الرحلات والفواتير والاعتراضات)
            composable(Destinations.TRIP_HISTORY) {
                TripHistoryScreen(
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 8. TECHNICAL SUPPORT SCREEN (الدعم الفني والمساعدة و SOS)
            composable(Destinations.SUPPORT) {
                SupportScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 9. ACCOUNT SCREEN (الحساب الشخصي والمحفظة والتقارير)
            composable(Destinations.ACCOUNT) {
                AccountScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Force Update Modal Overlay
        if (currentTariff.forceUpdateEnabled && !adminBypassActive) {
            ForceUpdateDialog(
                tariffConfig = currentTariff,
                onAdminBypass = {
                    adminBypassActive = true
                    navController.navigate(Destinations.ADMIN_PRICING)
                }
            )
        }
    }
}
