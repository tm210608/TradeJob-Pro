package com.tradejob.pro.home.domain

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.InvoiceEntity
import com.tradejob.pro.database.data.entity.UserEntity
import com.tradejob.pro.home.ui.invoices.InvoiceStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GenerateInvoicePdfUseCase @Inject constructor() {

    operator fun invoke(
        context: Context,
        client: ClientEntity,
        invoice: InvoiceEntity,
        professional: UserEntity? = null
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
        val titleText = "FACTURA"
        val titleWidth = paint.measureText(titleText)
        canvas.drawText(titleText, 595f - margin - titleWidth, 60f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("N: ${invoice.number}", 595f - margin - 100f, 78f, paint)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        canvas.drawText("Fecha: ${dateFormat.format(Date(invoice.issuedAt))}", 595f - margin - 100f, 92f, paint)

        // Status badge
        val status = InvoiceStatus.fromValue(invoice.status)
        paint.isFakeBoldText = true
        paint.color = when (status) {
            InvoiceStatus.PAID -> Color.rgb(0, 128, 0)
            InvoiceStatus.OVERDUE -> Color.RED
            InvoiceStatus.PENDING -> Color.rgb(200, 150, 0)
            InvoiceStatus.CANCELLED -> Color.GRAY
        }
        val statusDisplayName = when (status) {
            InvoiceStatus.PENDING -> "Pendiente"
            InvoiceStatus.PAID -> "Pagada"
            InvoiceStatus.OVERDUE -> "Vencida"
            InvoiceStatus.CANCELLED -> "Anulada"
        }
        canvas.drawText(statusDisplayName, 595f - margin - 100f, 108f, paint)
        paint.color = Color.BLACK
        paint.isFakeBoldText = false

        // Client section
        currentY = 150f
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("FACTURAR A:", margin, currentY, paint)

        currentY += 20f
        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText(client.name, margin, currentY, paint)
        currentY += 15f
        canvas.drawText("Tel: ${client.phone}", margin, currentY, paint)
        client.address?.let { currentY += 15f; canvas.drawText(it, margin, currentY, paint) }
        client.email?.let { currentY += 15f; canvas.drawText(it, margin, currentY, paint) }

        // Totals box
        currentY += 40f
        paint.textSize = 11f
        canvas.drawText("Subtotal:", 400f, currentY, paint)
        canvas.drawText("%.2f".format(invoice.subtotal), 510f, currentY, paint)

        currentY += 15f
        canvas.drawText("IVA (${invoice.vatRate.toInt()}%):", 400f, currentY, paint)
        canvas.drawText("%.2f".format(invoice.vatAmount), 510f, currentY, paint)

        invoice.irpfRate?.let { irpf ->
            currentY += 15f
            val irpfAmount = invoice.subtotal * irpf / 100.0
            canvas.drawText("IRPF (-${irpf.toInt()}%):", 400f, currentY, paint)
            canvas.drawText("-%.2f".format(irpfAmount), 510f, currentY, paint)
        }

        currentY += 25f
        canvas.drawLine(390f, currentY, 545f, currentY, paint)
        currentY += 20f

        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL:", 400f, currentY, paint)
        canvas.drawText("%.2f".format(invoice.total), 510f, currentY, paint)

        // Payment info
        currentY += 35f
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("ESTADO DE PAGO:", margin, currentY, paint)

        currentY += 18f
        paint.isFakeBoldText = false
        if (invoice.paidAmount > 0) {
            canvas.drawText("Pagado: %.2f".format(invoice.paidAmount), margin, currentY, paint)
            currentY += 15f
            canvas.drawText("Pendiente: %.2f".format(invoice.total - invoice.paidAmount), margin, currentY, paint)
        } else {
            canvas.drawText("Pendiente de pago", margin, currentY, paint)
        }

        invoice.paymentMethod?.let { method ->
            currentY += 15f
            canvas.drawText("Metodo de pago: $method", margin, currentY, paint)
        }

        invoice.dueAt?.let {
            currentY += 15f
            canvas.drawText("Vencimiento: ${dateFormat.format(Date(it))}", margin, currentY, paint)
        }

        // Notes
        invoice.notes?.let { notes ->
            if (notes.isNotBlank()) {
                currentY += 25f
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

        // Footer
        paint.textSize = 8f
        paint.color = Color.GRAY
        canvas.drawText("Generado desde TradeJob Pro", margin, 820f, paint)

        pdfDocument.finishPage(page)

        val fileName = "Factura_${invoice.number}_${System.currentTimeMillis()}.pdf"
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
