package com.tradejob.pro.home.ui.jobs

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val defaultLocale: Locale = Locale.forLanguageTag("es-ES")

// "13 nov" — formato corto que cabe en la tarjeta del parte.
fun formatJobDate(scheduledAt: Long?, locale: Locale = defaultLocale): String? {
    if (scheduledAt == null) return null
    return SimpleDateFormat("d MMM", locale).format(Date(scheduledAt))
}

// finalAmount tiene prioridad sobre budgetAmount. "1.250 €" / "1.250,50 €".
fun formatJobAmount(
    finalAmount: Double?,
    budgetAmount: Double?,
    locale: Locale = defaultLocale
): String? {
    val amount = finalAmount ?: budgetAmount ?: return null
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return "${formatter.format(amount)} €"
}

fun isUrgent(priority: JobPriority): Boolean =
    priority == JobPriority.HIGH || priority == JobPriority.URGENT
