package com.tradejob.pro.home.ui.jobs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class JobFormattersTest {

    private val es = Locale.forLanguageTag("es-ES")

    private fun millis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `formatJobDate devuelve dia y mes corto`() {
        assertEquals("13 nov", formatJobDate(millis(2026, Calendar.NOVEMBER, 13), es))
    }

    @Test
    fun `formatJobDate devuelve null sin fecha`() {
        assertNull(formatJobDate(null, es))
    }

    @Test
    fun `formatJobAmount prefiere finalAmount sobre budgetAmount`() {
        assertEquals("150 €", formatJobAmount(150.0, 90.0, es))
    }

    @Test
    fun `formatJobAmount usa budgetAmount si no hay finalAmount`() {
        assertEquals("90 €", formatJobAmount(null, 90.0, es))
    }

    @Test
    fun `formatJobAmount devuelve null sin importes`() {
        assertNull(formatJobAmount(null, null, es))
    }

    @Test
    fun `formatJobAmount formatea miles y decimales en es-ES`() {
        assertEquals("1.250,5 €", formatJobAmount(1250.50, null, es))
    }

    @Test
    fun `isUrgent solo para HIGH y URGENT`() {
        assertTrue(isUrgent(JobPriority.HIGH))
        assertTrue(isUrgent(JobPriority.URGENT))
        assertFalse(isUrgent(JobPriority.MEDIUM))
        assertFalse(isUrgent(JobPriority.LOW))
    }
}
