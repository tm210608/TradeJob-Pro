package com.tradejob.pro.components.resources

import androidx.compose.ui.graphics.Color

// Shim de compatibilidad (SPEC-002): esta paleta apunta ahora a los colores de
// marca "Técnico premium" (ver theme/Color.kt). Las pantallas deben migrar a
// MaterialTheme.colorScheme en las specs ui-*. No añadir nuevos usos.

// Primary Colors → petróleo
val PrimaryColor = Color(0xFF0C4A52)
val PrimaryDarkColor = Color(0xFF0A3B40)
val PrimaryLightColor = Color(0xFFB9EBF0)

// Secondary/Accent Colors → lima
val SecondaryColor = Color(0xFF617D00)
val SecondaryDarkColor = Color(0xFF425900)
val SecondaryLightColor = Color(0xFFD5F28C)

// Background Colors
val BackgroundLight = Color(0xFFF4F9F8)
val BackgroundDark = Color(0xFF0A191B)
val SurfaceLight = Color(0xFFF4F9F8)
val SurfaceDark = Color(0xFF26494C)

// Text Colors
val TextPrimaryLight = Color(0xFF10282A)
val TextSecondaryLight = Color(0xFF3F5557)
val TextPrimaryDark = Color(0xFFD9E8E8)
val TextSecondaryDark = Color(0xFFA9C4C5)

// Error Colors
val ErrorColor = Color(0xFFBA1A1A)
val ErrorLightColor = Color(0xFFFFDAD6)

// Success Colors → lima de marca (estados OK/pagado)
val SuccessColor = Color(0xFF617D00)
val SuccessLightColor = Color(0xFFD5F28C)

// Warning Colors
val WarningColor = Color(0xFFFFC107)
val WarningLightColor = Color(0xFFFFE082)

// Divider and Border
val DividerColor = Color(0xFFDCE7E7)
val BorderColor = Color(0xFF6F8B8C)
