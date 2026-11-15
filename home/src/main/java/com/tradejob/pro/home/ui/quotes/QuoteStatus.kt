package com.tradejob.pro.home.ui.quotes

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tradejob.pro.home.R

enum class QuoteStatus(val value: String, val displayRes: Int) {
    DRAFT("DRAFT", R.string.quote_status_draft),
    SENT("SENT", R.string.quote_status_sent),
    ACCEPTED("ACCEPTED", R.string.quote_status_accepted),
    REJECTED("REJECTED", R.string.quote_status_rejected);

    companion object {
        fun fromValue(value: String): QuoteStatus = entries.find { it.value == value } ?: DRAFT
    }
}

// Colores del parte: borrador neutro, enviado petróleo, aceptado lima, rechazado error.
val QuoteStatus.displayColor: Color
    @Composable get() = when (this) {
        QuoteStatus.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
        QuoteStatus.SENT -> MaterialTheme.colorScheme.primary
        QuoteStatus.ACCEPTED -> MaterialTheme.colorScheme.tertiary
        QuoteStatus.REJECTED -> MaterialTheme.colorScheme.error
    }
