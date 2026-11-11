package com.tradejob.pro.components.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TradeJobTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) TradeJobDarkScheme else TradeJobLightScheme,
        motionScheme = MotionScheme.expressive()
    ) {
        // Default global (SPEC-005): nav bar del color del fondo para OEMs
        // con barra opaca (MIUI/HyperOS) e iconos coherentes con el tema.
        // Las pantallas pueden sobreescribirlo (p. ej. login → petróleo).
        SystemBarIconStyle(
            darkIcons = !darkTheme,
            navigationBarColor = MaterialTheme.colorScheme.background
        )
        content()
    }
}

// Estilo de iconos de las barras del sistema por pantalla (edge-to-edge).
// darkIcons = true → iconos oscuros para fondos claros; false → claros para
// fondos oscuros (p. ej. el petróleo del login). Se restaura al salir.
@Composable
fun SystemBarIconStyle(darkIcons: Boolean, navigationBarColor: Color? = null) {
    val view = LocalView.current
    // darkTheme en las claves: al conmutar light↔dark el sistema, los efectos
    // se re-aplican en orden de composición y el override de una pantalla
    // (hija del tema) vuelve a ganar sobre el default global.
    val darkTheme = isSystemInDarkTheme()
    if (!view.isInEditMode) {
        DisposableEffect(darkIcons, navigationBarColor, darkTheme) {
            val window = (view.context as? Activity)?.window
                ?: return@DisposableEffect onDispose {}
            val controller = WindowCompat.getInsetsController(window, view)
            val prevStatus = controller.isAppearanceLightStatusBars
            val prevNav = controller.isAppearanceLightNavigationBars
            val prevNavColor = window.navigationBarColor
            controller.isAppearanceLightStatusBars = darkIcons
            controller.isAppearanceLightNavigationBars = darkIcons
            if (navigationBarColor != null) {
                // OEMs como MIUI/HyperOS dibujan la nav bar opaca ignorando la
                // transparencia de edge-to-edge: se pinta del color de fondo.
                window.isNavigationBarContrastEnforced = false
                window.navigationBarColor = navigationBarColor.toArgb()
            }
            onDispose {
                controller.isAppearanceLightStatusBars = prevStatus
                controller.isAppearanceLightNavigationBars = prevNav
                window.navigationBarColor = prevNavColor
            }
        }
    }
}
