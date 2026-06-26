package com.tradejob.pro.home.domain

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.ui.jobs.JobStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GeneratePdfReportUseCase @Inject constructor() {

    operator fun invoke(context: Context, client: ClientEntity, job: JobEntity): File? {
        val pdfDocument = PdfDocument()
        
        // Page info (A4 size in points: 595 x 842)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        
        val paint = Paint()
        val margin = 50f
        var currentY = 60f
        
        // Header
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("REPORTE DE TRABAJO", margin, currentY, paint)
        
        currentY += 20f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Generado el: ${dateFormat.format(Date())}", margin, currentY, paint)
        
        currentY += 40f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("DATOS DEL CLIENTE", margin, currentY, paint)
        
        currentY += 25f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Nombre: ${client.name}", margin, currentY, paint)
        currentY += 20f
        canvas.drawText("Teléfono: ${client.phone}", margin, currentY, paint)
        client.address?.let {
            currentY += 20f
            canvas.drawText("Dirección: $it", margin, currentY, paint)
        }
        
        currentY += 40f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("DETALLES DEL TRABAJO", margin, currentY, paint)
        
        currentY += 25f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Título: ${job.title}", margin, currentY, paint)
        
        currentY += 20f
        val status = JobStatus.fromValue(job.status).displayName
        canvas.drawText("Estado: $status", margin, currentY, paint)
        
        currentY += 20f
        canvas.drawText("Prioridad: ${job.priority}", margin, currentY, paint)
        
        job.scheduledAt?.let {
            currentY += 20f
            val scheduleFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            canvas.drawText("Programado para: ${scheduleFormat.format(Date(it))}", margin, currentY, paint)
        }
        
        if (!description.isNullOrBlank()) {
            currentY += 30f
            paint.isFakeBoldText = true
            canvas.drawText("Descripción:", margin, currentY, paint)
            currentY += 20f
            paint.isFakeBoldText = false
            
            // Mejorar el wrap de texto básico
            val maxWidth = 500f
            val words = description.split(" ")
            var line = StringBuilder()
            
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "${line} $word"
                val testWidth = paint.measureText(testLine)
                
                if (testWidth > maxWidth) {
                    canvas.drawText(line.toString(), margin + 10, currentY, paint)
                    currentY += 15f
                    line = StringBuilder(word)
                } else {
                    line = StringBuilder(testLine)
                }
                
                if (currentY > 780) { // Límite de página
                    canvas.drawText("... (continúa en la app)", margin + 10, currentY, paint)
                    break
                }
            }
            if (line.isNotEmpty() && currentY <= 780) {
                canvas.drawText(line.toString(), margin + 10, currentY, paint)
                currentY += 15f
            }
        }
        
        currentY += 30f
        job.budgetAmount?.let {
            canvas.drawText("Presupuesto estimado: $it €", margin, currentY, paint)
            currentY += 20f
        }
        job.finalAmount?.let {
            paint.isFakeBoldText = true
            canvas.drawText("Importe Final: $it €", margin, currentY, paint)
            paint.isFakeBoldText = false
        }
        
        // Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Generado desde TradeJob Pro", margin, 810f, paint)
        
        pdfDocument.finishPage(page)
        
        // Save to cache
        val fileName = "Reporte_${job.id}_${System.currentTimeMillis()}.pdf"
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
}
