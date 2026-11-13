package com.tradejob.pro.home.ui.clients

import com.tradejob.pro.database.data.entity.JobEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClientFormattersTest {

    private fun job(id: Long, status: String, createdAt: Long = 0L) = JobEntity(
        id = id,
        clientId = 1L,
        title = "Trabajo $id",
        description = "",
        status = status,
        priority = "MEDIUM",
        createdAt = createdAt
    )

    @Test
    fun `countJobsByStatus agrupa por estado`() {
        val jobs = listOf(
            job(1, "PENDING"),
            job(2, "IN_PROGRESS"),
            job(3, "COMPLETED"),
            job(4, "INVOICED"),
            job(5, "PENDING")
        )
        val counts = countJobsByStatus(jobs)
        assertEquals(1, counts.inProgress)
        assertEquals(2, counts.pending)
        assertEquals(2, counts.completed) // COMPLETED + INVOICED como el dashboard
    }

    @Test
    fun `countJobsByStatus lista vacia da ceros`() {
        val counts = countJobsByStatus(emptyList())
        assertEquals(0, counts.inProgress)
        assertEquals(0, counts.pending)
        assertEquals(0, counts.completed)
    }

    @Test
    fun `lastJob devuelve el de createdAt mayor`() {
        val jobs = listOf(
            job(1, "COMPLETED", createdAt = 100L),
            job(2, "PENDING", createdAt = 300L),
            job(3, "IN_PROGRESS", createdAt = 200L)
        )
        assertEquals(2L, lastJob(jobs)?.id)
    }

    @Test
    fun `lastJob con lista vacia devuelve null`() {
        assertNull(lastJob(emptyList()))
    }
}
