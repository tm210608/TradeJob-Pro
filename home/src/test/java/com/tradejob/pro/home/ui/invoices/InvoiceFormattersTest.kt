package com.tradejob.pro.home.ui.invoices

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class InvoiceFormattersTest {

    private val es = Locale.forLanguageTag("es-ES")

    private fun millis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `formatInvoiceDate devuelve dia y mes corto`() {
        assertEquals("15 nov", formatInvoiceDate(millis(2026, Calendar.NOVEMBER, 15), es))
    }

    @Test
    fun `formatInvoiceDate devuelve null sin fecha`() {
        assertNull(formatInvoiceDate(null, es))
    }

    @Test
    fun `formatInvoiceAmount entero sin decimales`() {
        assertEquals("890 €", formatInvoiceAmount(890.0, es))
    }

    @Test
    fun `formatInvoiceAmount formatea miles y decimales en es-ES`() {
        assertEquals("1.250,5 €", formatInvoiceAmount(1250.50, es))
    }

    @Test
    fun `formatInvoiceAmount con decimales`() {
        assertEquals("108,9 €", formatInvoiceAmount(108.90, es))
    }

    @Test
    fun `formatInvoiceAmount cero`() {
        assertEquals("0 €", formatInvoiceAmount(0.0, es))
    }
}
