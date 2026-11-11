package com.tradejob.pro.components.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}

// Estilo de iconos de las barras del sistema por pantalla (edge-to-edge).
// darkIcons = true → iconos oscuros para fondos claros; false → claros para
// fondos oscuros (p. ej. el petróleo del login). Se restaura al salir.
@Composable
fun SystemBarIconStyle(darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(darkIcons) {
            val window = (view.context as? Activity)?.window
                ?: return@DisposableEffect onDispose {}
            val controller = WindowCompat.getInsetsController(window, view)
            val prevStatus = controller.isAppearanceLightStatusBars
            val prevNav = controller.isAppearanceLightNavigationBars
            controller.isAppearanceLightStatusBars = darkIcons
            controller.isAppearanceLightNavigationBars = darkIcons
            onDispose {
                controller.isAppearanceLightStatusBars = prevStatus
                controller.isAppearanceLightNavigationBars = prevNav
            }
        }
    }
}
