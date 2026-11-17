package com.tradejob.pro.home.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DocumentNumberTest {

    @Test
    fun `format rellena con ceros a la izquierda`() {
        assertEquals("P-2026-0001", DocumentNumber.format("P", 2026, 1))
        assertEquals("F-2026-0042", DocumentNumber.format("F", 2026, 42))
    }

    @Test
    fun `format no trunca secuencias de mas de 4 digitos`() {
        assertEquals("P-2026-10000", DocumentNumber.format("P", 2026, 10000))
    }

    @Test
    fun `likePattern genera patron del ano`() {
        assertEquals("P-2026-%", DocumentNumber.likePattern("P", 2026))
        assertEquals("F-2027-%", DocumentNumber.likePattern("F", 2027))
    }

    @Test
    fun `parseSequence extrae el sufijo del numero`() {
        assertEquals(7, DocumentNumber.parseSequence("P-2026-0007", "P", 2026))
    }

    @Test
    fun `parseSequence devuelve null para formato antiguo y otros prefijos o anos`() {
        assertNull(DocumentNumber.parseSequence("P-1794772064", "P", 2026))
        assertNull(DocumentNumber.parseSequence("F-2026-0007", "P", 2026))
        assertNull(DocumentNumber.parseSequence("P-2025-0007", "P", 2026))
        assertNull(DocumentNumber.parseSequence("P-2026-", "P", 2026))
    }

    @Test
    fun `next empieza en 0001 sin numeros existentes`() {
        assertEquals("P-2026-0001", DocumentNumber.next("P", 2026, emptyList()))
    }

    @Test
    fun `next continua tras el maximo existente`() {
        val existing = listOf("P-2026-0001", "P-2026-0002", "P-2026-0010")
        assertEquals("P-2026-0011", DocumentNumber.next("P", 2026, existing))
    }

    @Test
    fun `next ignora numeros antiguos y de otros anos o prefijos`() {
        val existing = listOf("P-1794772064", "F-2026-0099", "P-2025-0099", "P-2026-0003")
        assertEquals("P-2026-0004", DocumentNumber.next("P", 2026, existing))
    }

    @Test
    fun `next no reutiliza el numero de un documento borrado`() {
        // Tras borrar P-2026-0002 queda solo P-2026-0001 y P-2026-0003:
        // el siguiente debe ser 0004, no 0002.
        val existing = listOf("P-2026-0001", "P-2026-0003")
        assertEquals("P-2026-0004", DocumentNumber.next("P", 2026, existing))
    }

    @Test
    fun `next no reutiliza el ultimo numero aunque el documento se borre`() {
        // Se emitio 0002 y despues se borro: existing solo tiene 0001,
        // pero lastIssued=2 impide reutilizar 0002.
        val existing = listOf("P-2026-0001")
        assertEquals("P-2026-0003", DocumentNumber.next("P", 2026, existing, lastIssued = 2))
    }

    @Test
    fun `next respeta lastIssued aunque no haya documentos existentes`() {
        assertEquals("F-2026-0003", DocumentNumber.next("F", 2026, emptyList(), lastIssued = 2))
    }
}
