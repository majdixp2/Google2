package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ==========================================
// THEME MANAGER
// ==========================================
object ThemeManager {
    private val _isDarkMode = MutableStateFlow(true) // Default to electric dark fleet theme
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }
}

// ==========================================
// APP COLORS DATA MODEL
// ==========================================
data class AppColors(
    val bg: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBg: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textTitle: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val blueTint: Color,
    val secondary: Color,
    val secondaryLight: Color,
    val secondaryDark: Color,
    val secondaryContainer: Color,
    val appleGreen: Color,
    val appleGreenLight: Color,
    val appleGreenContainer: Color,
    val paleYellow: Color,
    val paleYellowContainer: Color,
    val onPaleYellow: Color,
    val amber: Color,
    val amberLight: Color,
    val amberContainer: Color,
    val rose: Color,
    val roseLight: Color,
    val roseDark: Color,
    val roseContainer: Color,
    val indigo: Color,
    val indigoLight: Color,
    val indigoContainer: Color,
    val cyan: Color,
    val cyanContainer: Color,
    val isDark: Boolean
)

val LightColors = AppColors(
    bg = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    cardBg = LightCardBg,
    border = LightBorder,
    borderSubtle = LightBorderSubtle,
    textPrimary = LightTextPrimary,
    textTitle = LightTextTitle,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    primary = LightPrimary,
    primaryLight = LightPrimaryLight,
    primaryDark = LightPrimaryDark,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    blueTint = LightBlueTint,
    secondary = PolishSecondary,
    secondaryLight = PolishSecondaryLight,
    secondaryDark = PolishSecondaryDark,
    secondaryContainer = PolishSecondaryContainer,
    appleGreen = ElectricBlue,
    appleGreenLight = ElectricBlueLight,
    appleGreenContainer = LightPrimaryContainer,
    paleYellow = PaleYellow,
    paleYellowContainer = PaleYellowLight,
    onPaleYellow = OnPaleYellow,
    amber = PolishAmber,
    amberLight = PolishAmberLight,
    amberContainer = PolishAmberContainer,
    rose = PolishRose,
    roseLight = PolishRoseLight,
    roseDark = PolishRoseDark,
    roseContainer = PolishRoseContainer,
    indigo = PolishIndigo,
    indigoLight = PolishIndigoLight,
    indigoContainer = PolishIndigoContainer,
    cyan = PolishCyan,
    cyanContainer = PolishCyanContainer,
    isDark = false
)

// Theme 3: Electric Blue & Corporate Fleet (كحلي وسافير أسطول ذكي، أزرق كهربائي وسماوي مشرق)
val DarkColors = AppColors(
    bg = DarkNavyBg,
    surface = NavySurface,
    surfaceVariant = NavySurfaceVariant,
    cardBg = NavyCardBg,
    border = NavyBorder,
    borderSubtle = NavyBorderSubtle,
    textPrimary = DarkTextPrimary, // أبيض ناصع
    textTitle = DarkTextTitle,     // أبيض ناصع
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    primary = ElectricBlue,        // أزرق كهربائي
    primaryLight = ElectricBlueLight,
    primaryDark = ElectricBlueDark,
    primaryContainer = ElectricBlueContainer,
    onPrimaryContainer = OnElectricBlueContainer,
    blueTint = NavySurfaceVariant,
    secondary = ElectricBlueLight,
    secondaryLight = ElectricBlueLight,
    secondaryDark = ElectricBlueDark,
    secondaryContainer = ElectricBlueContainer,
    appleGreen = ElectricBlueLight, // العداد باللون السماوي والأزرق الكهربائي المضيء
    appleGreenLight = AppleGreenLight,
    appleGreenContainer = AppleGreenContainer,
    paleYellow = PaleYellow,        // ذهبي متناسق
    paleYellowContainer = PaleYellowLight,
    onPaleYellow = OnPaleYellow,
    amber = DarkAmber,
    amberLight = PolishAmberLight,
    amberContainer = DarkAmberContainer,
    rose = DarkRose,
    roseLight = DarkRoseLight,
    roseDark = DarkRoseDark,
    roseContainer = DarkRoseContainer,
    indigo = DarkIndigo,
    indigoLight = PolishIndigoLight,
    indigoContainer = DarkIndigoContainer,
    cyan = DarkCyan,
    cyanContainer = DarkCyanContainer,
    isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = PolishSecondary,
    onSecondary = Color.White,
    secondaryContainer = PolishSecondaryContainer,
    onSecondaryContainer = PolishSecondaryDark,
    tertiary = PolishAmber,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextTitle,
    surface = LightSurface,
    onSurface = LightTextTitle,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = PolishRose,
    onError = Color.White,
    errorContainer = PolishRoseContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueContainer,
    onPrimaryContainer = OnElectricBlueContainer,
    secondary = ElectricBlueLight,
    onSecondary = Color.White,
    secondaryContainer = ElectricBlueContainer,
    onSecondaryContainer = OnElectricBlueContainer,
    tertiary = PaleYellow,
    onTertiary = OnPaleYellow,
    background = DarkNavyBg,
    onBackground = DarkTextPrimary,
    surface = NavySurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = NavyBorder,
    error = DarkRose,
    onError = Color.White,
    errorContainer = DarkRoseContainer
)

@Composable
fun SmartRideMeterTheme(
    darkTheme: Boolean = ThemeManager.isDarkMode.collectAsState().value,
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkColors else LightColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
