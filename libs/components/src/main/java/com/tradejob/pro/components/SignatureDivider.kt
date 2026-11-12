package com.tradejob.pro.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Trazo de firma manuscrita — motivo gráfico de marca (SPEC-010).
// Una curva fluida con dos "letras" y rúbrica, como quien firma un parte de trabajo.
@Composable
fun MitoSignatureDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    width: Dp = 150.dp,
    height: Dp = 36.dp
) {
    Canvas(modifier = modifier.width(width).height(height)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = h * 0.09f, cap = StrokeCap.Round)

        val signature = Path().apply {
            // Primer trazo ascendente-descendente (inicial)
            moveTo(w * 0.06f, h * 0.78f)
            cubicTo(w * 0.10f, h * 0.30f, w * 0.16f, h * 0.12f, w * 0.22f, h * 0.30f)
            cubicTo(w * 0.28f, h * 0.48f, w * 0.26f, h * 0.72f, w * 0.34f, h * 0.70f)
            // Segunda letra enlazada
            cubicTo(w * 0.42f, h * 0.68f, w * 0.44f, h * 0.38f, w * 0.52f, h * 0.38f)
            cubicTo(w * 0.60f, h * 0.38f, w * 0.60f, h * 0.68f, w * 0.68f, h * 0.62f)
            // Cola hacia la rúbrica
            cubicTo(w * 0.76f, h * 0.56f, w * 0.80f, h * 0.40f, w * 0.88f, h * 0.38f)
        }
        drawPath(signature, color = color, style = stroke)

        // Rúbrica: subrayado fluido
        val flourish = Path().apply {
            moveTo(w * 0.18f, h * 0.88f)
            cubicTo(w * 0.42f, h * 0.94f, w * 0.66f, h * 0.94f, w * 0.92f, h * 0.82f)
        }
        drawPath(flourish, color = color.copy(alpha = 0.75f), style = Stroke(width = h * 0.07f, cap = StrokeCap.Round))
    }
}
