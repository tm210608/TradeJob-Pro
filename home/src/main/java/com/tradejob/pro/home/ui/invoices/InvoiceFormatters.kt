package com.tradejob.pro.home.ui.invoices

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val defaultLocale: Locale = Locale.forLanguageTag("es-ES")

// "15 nov" — fecha de emisión corta para la tarjeta, mismo formato que formatQuoteDate.
fun formatInvoiceDate(issuedAt: Long?, locale: Locale = defaultLocale): String? {
    if (issuedAt == null) return null
    return SimpleDateFormat("d MMM", locale).format(Date(issuedAt))
}

// "1.250 €" / "108,90 €" — mismo formato es-ES que formatQuoteAmount.
fun formatInvoiceAmount(total: Double, locale: Locale = defaultLocale): String {
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return "${formatter.format(total)} €"
}
