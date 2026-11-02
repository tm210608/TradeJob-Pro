package com.tradejob.pro.components.theme

import androidx.compose.ui.graphics.Color

// Dirección "Técnico premium" (SPEC-002): petróleo profundo + lima eléctrico.
// La lima es acento quirúrgico: CTAs, FAB, estados OK/pagado — nunca fondos grandes.

val Petrol10 = Color(0xFF032326)
val Petrol20 = Color(0xFF0A3B40)
val Petrol30 = Color(0xFF0C4A52)
val Petrol40 = Color(0xFF0F5A64)
val Petrol80 = Color(0xFF7ED4DC)
val Petrol90 = Color(0xFFB9EBF0)

val Slate50 = Color(0xFFF4F9F8)
val Slate200 = Color(0xFFDCE7E7)
val Slate400 = Color(0xFF6F8B8C)
val Slate600 = Color(0xFF3F5557)
val Slate800 = Color(0xFF26494C)
val Slate900 = Color(0xFF0A191B)

val LimeAccent = Color(0xFFB9E62E)
val LimeContainerLight = Color(0xFFD5F28C)
val LimeOnContainerLight = Color(0xFF253000)
val LimeDark = Color(0xFF617D00)
val LimeContainerDark = Color(0xFF425900)
val LimeOnContainerDark = Color(0xFFD5F28C)

val TradeJobLightScheme = androidx.compose.material3.lightColorScheme(
    primary = Petrol30,
    onPrimary = Color.White,
    primaryContainer = Petrol90,
    onPrimaryContainer = Petrol10,
    secondary = Color(0xFF48676B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC9E2E4),
    onSecondaryContainer = Petrol10,
    tertiary = LimeDark,
    onTertiary = Color.White,
    tertiaryContainer = LimeContainerLight,
    onTertiaryContainer = LimeOnContainerLight,
    background = Slate50,
    onBackground = Color(0xFF10282A),
    surface = Slate50,
    onSurface = Color(0xFF10282A),
    surfaceVariant = Slate200,
    onSurfaceVariant = Slate600,
    outline = Slate400,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val TradeJobDarkScheme = androidx.compose.material3.darkColorScheme(
    primary = Petrol80,
    onPrimary = Petrol10,
    primaryContainer = Petrol30,
    onPrimaryContainer = Petrol90,
    secondary = Color(0xFFA8CCCF),
    onSecondary = Color(0xFF0F3136),
    secondaryContainer = Color(0xFF2C4A4E),
    onSecondaryContainer = Color(0xFFC9E2E4),
    tertiary = LimeAccent,
    onTertiary = LimeOnContainerLight,
    tertiaryContainer = LimeContainerDark,
    onTertiaryContainer = LimeOnContainerDark,
    background = Slate900,
    onBackground = Color(0xFFD9E8E8),
    surface = Slate900,
    onSurface = Color(0xFFD9E8E8),
    surfaceVariant = Slate800,
    onSurfaceVariant = Color(0xFFA9C4C5),
    outline = Slate400,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)
