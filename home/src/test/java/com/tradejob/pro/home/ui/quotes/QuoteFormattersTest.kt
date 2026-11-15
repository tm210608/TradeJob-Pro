package com.tradejob.pro.home.ui.quotes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class QuoteFormattersTest {

    private val es = Locale.forLanguageTag("es-ES")

    private fun millis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `formatQuoteDate devuelve dia y mes corto`() {
        assertEquals("13 nov", formatQuoteDate(millis(2026, Calendar.NOVEMBER, 13), es))
    }

    @Test
    fun `formatQuoteDate devuelve null sin fecha`() {
        assertNull(formatQuoteDate(null, es))
    }

    @Test
    fun `formatQuoteAmount entero sin decimales`() {
        assertEquals("890 €", formatQuoteAmount(890.0, es))
    }

    @Test
    fun `formatQuoteAmount formatea miles y decimales en es-ES`() {
        assertEquals("1.250,5 €", formatQuoteAmount(1250.50, es))
    }

    @Test
    fun `formatQuoteAmount cero`() {
        assertEquals("0 €", formatQuoteAmount(0.0, es))
    }

    @Test
    fun `formatQuoteQuantity entero sin decimales`() {
        assertEquals("2", formatQuoteQuantity(2.0, es))
    }

    @Test
    fun `formatQuoteQuantity con decimales`() {
        assertEquals("1,5", formatQuoteQuantity(1.5, es))
    }
}
