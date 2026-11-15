package com.tradejob.pro.home.ui.quotes

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val defaultLocale: Locale = Locale.forLanguageTag("es-ES")

// "13 nov" — fecha de creación corta para la tarjeta, mismo formato que formatJobDate.
fun formatQuoteDate(createdAt: Long?, locale: Locale = defaultLocale): String? {
    if (createdAt == null) return null
    return SimpleDateFormat("d MMM", locale).format(Date(createdAt))
}

// "1.250 €" / "1.250,5 €" — mismo formato es-ES que formatJobAmount.
fun formatQuoteAmount(total: Double, locale: Locale = defaultLocale): String {
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return "${formatter.format(total)} €"
}

// Cantidad de partida sin símbolo: "2" en vez de "2.0".
fun formatQuoteQuantity(quantity: Double, locale: Locale = defaultLocale): String {
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return formatter.format(quantity)
}
