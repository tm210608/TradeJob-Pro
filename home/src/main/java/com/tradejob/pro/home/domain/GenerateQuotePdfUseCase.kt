package com.tradejob.pro.home.domain

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.QuoteEntity
import com.tradejob.pro.database.data.entity.QuoteItemEntity
import com.tradejob.pro.database.data.entity.UserEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GenerateQuotePdfUseCase @Inject constructor() {

    operator fun invoke(
        context: Context,
        client: ClientEntity,
        quote: QuoteEntity,
        items: List<QuoteItemEntity>,
        professional: UserEntity? = null,
        signaturePath: String? = null
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val margin = 50f
        var currentY = 60f

        // Professional header (left)
        professional?.let { prof ->
            paint.color = Color.BLACK
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText(prof.businessName ?: prof.name, margin, currentY, paint)

            currentY += 16f
            paint.textSize = 9f
            paint.isFakeBoldText = false
            prof.nif?.let { canvas.drawText("NIF: $it", margin, currentY, paint); currentY += 11f }
            prof.businessAddress?.let { canvas.drawText(it, margin, currentY, paint); currentY += 11f }
            prof.businessCity?.let { canvas.drawText(it, margin, currentY, paint); currentY += 11f }
            (prof.businessPhone ?: prof.phone)?.let { canvas.drawText("Tel: $it", margin, currentY, paint); currentY += 11f }
            canvas.drawText(prof.email, margin, currentY, paint)
        }

        // Title (right)
        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.isFakeBoldText = true
        val titleText = "PRESUPUESTO"
        val titleWidth = paint.measureText(titleText)
        canvas.drawText(titleText, 595f - margin - titleWidth, 60f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("N: ${quote.number}", 595f - margin - 100f, 78f, paint)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        canvas.drawText("Fecha: ${dateFormat.format(Date(quote.createdAt))}", 595f - margin - 100f, 92f, paint)

        // Client section
        currentY = 150f
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("DATOS DEL CLIENTE", margin, currentY, paint)

        currentY += 20f
        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText(client.name, margin, currentY, paint)
        currentY += 15f
        canvas.drawText("Tel: ${client.phone}", margin, currentY, paint)
        client.address?.let { currentY += 15f; canvas.drawText(it, margin, currentY, paint) }
        client.city?.let { currentY += 15f; canvas.drawText(it, margin, currentY, paint) }

        // Items table header
        currentY += 35f
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("PARTIDAS", margin, currentY, paint)

        currentY += 20f
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Descripcion", margin, currentY, paint)
        canvas.drawText("Cant.", 350f, currentY, paint)
        canvas.drawText("Precio", 410f, currentY, paint)
        canvas.drawText("Total", 500f, currentY, paint)

        currentY += 5f
        canvas.drawLine(margin, currentY, 545f, currentY, paint)
        currentY += 15f

        paint.isFakeBoldText = false
        items.forEach { item ->
            if (currentY > 650f) return@forEach

            val descLines = wrapText(item.description, paint, 280f)
            descLines.forEachIndexed { index, line ->
                canvas.drawText(line, margin, currentY + (index * 13f), paint)
            }
            val rowHeight = maxOf(descLines.size * 13f, 15f)

            canvas.drawText("${item.quantity}", 360f, currentY, paint)
            canvas.drawText("%.2f".format(item.unitPrice), 410f, currentY, paint)
            paint.isFakeBoldText = true
            canvas.drawText("%.2f".format(item.total), 500f, currentY, paint)
            paint.isFakeBoldText = false

            currentY += rowHeight + 5f
        }

        // Totals
        currentY += 20f
        canvas.drawLine(margin, currentY, 545f, currentY, paint)
        currentY += 20f

        paint.textSize = 11f
        canvas.drawText("Subtotal:", 400f, currentY, paint)
        canvas.drawText("%.2f".format(quote.subtotal), 510f, currentY, paint)

        currentY += 15f
        canvas.drawText("IVA (${quote.vatRate.toInt()}%):", 400f, currentY, paint)
        canvas.drawText("%.2f".format(quote.vatAmount), 510f, currentY, paint)

        currentY += 20f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL:", 400f, currentY, paint)
        canvas.drawText("%.2f".format(quote.total), 510f, currentY, paint)

        // Notes
        quote.notes?.let { notes ->
            if (notes.isNotBlank()) {
                currentY += 30f
                paint.textSize = 10f
                paint.isFakeBoldText = true
                canvas.drawText("Notas:", margin, currentY, paint)
                currentY += 15f
                paint.isFakeBoldText = false
                val noteLines = wrapText(notes, paint, 495f)
                noteLines.forEach { line ->
                    if (currentY > 780f) return@forEach
                    canvas.drawText(line, margin, currentY, paint)
                    currentY += 13f
                }
            }
        }

        // Signature
        if (signaturePath != null) {
            val sigFile = File(signaturePath)
            if (sigFile.exists()) {
                currentY = 740f
                paint.textSize = 9f
                paint.color = Color.GRAY
                canvas.drawText("Firma del cliente:", margin, currentY, paint)
                try {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(signaturePath)
                    if (bitmap != null) {
                        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 180, 90, false)
                        canvas.drawBitmap(scaledBitmap, margin, currentY + 5f, paint)
                        if (scaledBitmap != bitmap) scaledBitmap.recycle()
                        bitmap.recycle()
                    }
                } catch (e: Exception) { /* ignore */ }
            }
        }

        // Footer
        paint.textSize = 8f
        paint.color = Color.GRAY
        canvas.drawText("Generado desde TradeJob Pro", margin, 820f, paint)

        pdfDocument.finishPage(page)

        val fileName = "Presupuesto_${quote.number}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var line = StringBuilder()

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth) {
                if (line.isNotEmpty()) lines.add(line.toString())
                line = StringBuilder(word)
            } else {
                line = StringBuilder(testLine)
            }
        }
        if (line.isNotEmpty()) lines.add(line.toString())
        return lines
    }
}
