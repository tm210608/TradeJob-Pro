package com.tradejob.pro.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

data class StrokePoint(val x: Float, val y: Float)
data class SignatureStroke(val points: List<StrokePoint>)

@Composable
fun SignaturePad(
    onSignatureSaved: (File) -> Unit,
    modifier: Modifier = Modifier,
    context: Context,
    hintText: String = "Firme aqui",
    clearText: String = "Limpiar",
    saveText: String = "Guardar firma"
) {
    val strokes = remember { mutableStateListOf<SignatureStroke>() }
    var currentStroke by remember { mutableStateOf<SignatureStroke?>(null) }
    var hasStrokes by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke = SignatureStroke(listOf(StrokePoint(offset.x, offset.y)))
                        },
                        onDrag = { change, _ ->
                            currentStroke?.let { stroke ->
                                val newPoints = stroke.points + StrokePoint(change.position.x, change.position.y)
                                currentStroke = SignatureStroke(newPoints)
                            }
                        },
                        onDragEnd = {
                            currentStroke?.let { stroke ->
                                if (stroke.points.size > 1) {
                                    strokes.add(stroke)
                                    hasStrokes = true
                                }
                            }
                            currentStroke = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                strokes.forEach { stroke ->
                    drawStroke(stroke)
                }
                currentStroke?.let { stroke ->
                    drawStroke(stroke)
                }
            }

            if (!hasStrokes) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hintText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    strokes.clear()
                    hasStrokes = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(clearText)
            }

            Button(
                onClick = {
                    val file = saveSignatureToFile(context, strokes)
                    if (file != null) {
                        onSignatureSaved(file)
                    }
                },
                enabled = hasStrokes,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(saveText)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStroke(stroke: SignatureStroke) {
    if (stroke.points.size < 2) return

    val path = ComposePath()
    path.moveTo(stroke.points[0].x, stroke.points[0].y)

    for (i in 1 until stroke.points.size) {
        path.lineTo(stroke.points[i].x, stroke.points[i].y)
    }

    drawPath(
        path = path,
        color = androidx.compose.ui.graphics.Color.Black,
        style = Stroke(width = 3.dp.toPx())
    )
}

private fun saveSignatureToFile(context: Context, strokes: List<SignatureStroke>): File? {
    if (strokes.isEmpty()) return null

    val width = 600
    val height = 300
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 4f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    strokes.forEach { stroke ->
        if (stroke.points.size < 2) return@forEach

        val path = Path()
        val scaleX = width.toFloat() / 1080f
        val scaleY = height.toFloat() / 400f

        path.moveTo(stroke.points[0].x * scaleX, stroke.points[0].y * scaleY)

        for (i in 1 until stroke.points.size) {
            val prev = stroke.points[i - 1]
            val curr = stroke.points[i]
            val midX = (prev.x + curr.x) / 2 * scaleX
            val midY = (prev.y + curr.y) / 2 * scaleY
            path.quadTo(prev.x * scaleX, prev.y * scaleY, midX, midY)
        }

        canvas.drawPath(path, paint)
    }

    val signaturesDir = File(context.filesDir, "signatures")
    if (!signaturesDir.exists()) signaturesDir.mkdirs()

    val file = File(signaturesDir, "signature_${System.currentTimeMillis()}.png")

    return try {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        file
    } catch (e: Exception) {
        bitmap.recycle()
        null
    }
}
