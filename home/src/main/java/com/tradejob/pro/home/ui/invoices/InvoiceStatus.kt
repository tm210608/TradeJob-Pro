package com.tradejob.pro.home.ui.invoices

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tradejob.pro.components.theme.AmberWarning
import com.tradejob.pro.components.theme.AmberWarningDark
import com.tradejob.pro.home.R

enum class InvoiceStatus(val value: String, val displayRes: Int) {
    PENDING("PENDING", R.string.invoice_status_pending),
    PAID("PAID", R.string.invoice_status_paid),
    OVERDUE("OVERDUE", R.string.invoice_status_overdue),
    CANCELLED("CANCELLED", R.string.invoice_status_cancelled);

    companion object {
        fun fromValue(value: String): InvoiceStatus = entries.find { it.value == value } ?: PENDING
    }
}

// Colores del parte: pendiente ámbar, pagada lima, vencida error, anulada neutra.
val InvoiceStatus.displayColor: Color
    @Composable get() = when (this) {
        InvoiceStatus.PENDING -> if (isSystemInDarkTheme()) AmberWarningDark else AmberWarning
        InvoiceStatus.PAID -> MaterialTheme.colorScheme.tertiary
        InvoiceStatus.OVERDUE -> MaterialTheme.colorScheme.error
        InvoiceStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
