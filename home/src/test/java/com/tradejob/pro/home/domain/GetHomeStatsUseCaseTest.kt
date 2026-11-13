package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.InvoiceEntity
import com.tradejob.pro.database.data.entity.JobEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GetHomeStatsUseCaseTest {

    private val clientRepository: ClientRepository = mockk()
    private val jobRepository: JobRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val useCase = GetHomeStatsUseCase(clientRepository, jobRepository, invoiceRepository)

    // Reloj fijo: miercoles 11-11-2026 al mediodia (semana lun 9 – dom 15).
    private fun at(year: Int, month: Int, day: Int, hour: Int = 0): Long =
        Calendar.getInstance().apply {
            set(year, month, day, hour, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private val now = at(2026, Calendar.NOVEMBER, 11, 12)
    private val inWeek = at(2026, Calendar.NOVEMBER, 10, 9)
    private val sundayInWeek = at(2026, Calendar.NOVEMBER, 15, 22)
    private val lastWeek = at(2026, Calendar.NOVEMBER, 8, 22)
    private val nextWeek = at(2026, Calendar.NOVEMBER, 16, 9)

    @Before
    fun setUp() {
        useCase.nowProvider = { now }
    }

    private fun stub(
        clients: List<ClientEntity> = emptyList(),
        jobs: List<JobEntity> = emptyList(),
        invoices: List<InvoiceEntity> = emptyList()
    ) {
        every { clientRepository.getAllClients() } returns flowOf(Result.Success(clients))
        every { jobRepository.getAllJobs() } returns flowOf(Result.Success(jobs))
        every { invoiceRepository.getAllInvoices() } returns flowOf(Result.Success(invoices))
    }

    private suspend fun stats(vararg jobs: JobEntity, clients: List<ClientEntity> = emptyList(), invoices: List<InvoiceEntity> = emptyList()): HomeStats {
        stub(clients = clients, jobs = jobs.toList(), invoices = invoices)
        return (useCase().first() as Result.Success).data
    }

    @Test
    fun `trabajos esta semana cuenta scheduledAt dentro de la semana`() = runTest {
        val s = stats(
            JobEntity(id = 1, clientId = 1, title = "a", scheduledAt = inWeek),
            JobEntity(id = 2, clientId = 1, title = "b", scheduledAt = sundayInWeek),
            JobEntity(id = 3, clientId = 1, title = "c", scheduledAt = nextWeek),
            JobEntity(id = 4, clientId = 1, title = "d", scheduledAt = lastWeek)
        )
        assertEquals(2, s.jobsThisWeek)
    }

    @Test
    fun `trabajo sin scheduledAt cuenta por createdAt`() = runTest {
        val s = stats(
            JobEntity(id = 1, clientId = 1, title = "a", scheduledAt = null, createdAt = inWeek),
            JobEntity(id = 2, clientId = 1, title = "b", scheduledAt = null, createdAt = lastWeek)
        )
        assertEquals(1, s.jobsThisWeek)
    }

    @Test
    fun `terminados esta semana usa completedAt`() = runTest {
        val s = stats(
            JobEntity(id = 1, clientId = 1, title = "a", status = "COMPLETED", completedAt = inWeek),
            JobEntity(id = 2, clientId = 1, title = "b", status = "COMPLETED", completedAt = lastWeek),
            JobEntity(id = 3, clientId = 1, title = "c", status = "IN_PROGRESS")
        )
        assertEquals(1, s.completedThisWeek)
        assertTrue(s.hasCelebration)
    }

    @Test
    fun `facturas cobradas esta semana solo PAID con paidAt en semana`() = runTest {
        val s = stats(
            invoices = listOf(
                InvoiceEntity(id = 1, clientId = 1, status = "PAID", paidAt = inWeek),
                InvoiceEntity(id = 2, clientId = 1, status = "PAID", paidAt = lastWeek),
                InvoiceEntity(id = 3, clientId = 1, status = "PENDING", paidAt = inWeek),
                InvoiceEntity(id = 4, clientId = 1, status = "PAID", paidAt = null)
            )
        )
        assertEquals(1, s.paidInvoicesThisWeek)
    }

    @Test
    fun `terminados totales cuentan COMPLETED e INVOICED`() = runTest {
        val s = stats(
            JobEntity(id = 1, clientId = 1, title = "a", status = "COMPLETED"),
            JobEntity(id = 2, clientId = 1, title = "b", status = "INVOICED"),
            JobEntity(id = 3, clientId = 1, title = "c", status = "PENDING"),
            JobEntity(id = 4, clientId = 1, title = "d", status = "IN_PROGRESS")
        )
        assertEquals(2, s.completedJobs)
        assertEquals(1, s.pendingJobs)
        assertEquals(1, s.inProgressJobs)
        assertEquals(4, s.totalJobs)
    }

    @Test
    fun `sin clientes ni trabajos el panel queda vacio y sin celebracion`() = runTest {
        val s = stats()
        assertTrue(s.isEmpty)
        assertFalse(s.hasCelebration)
        assertEquals(0, s.jobsThisWeek)
    }

    @Test
    fun `currentWeekRange empieza en lunes y excluye el lunes siguiente`() {
        val week = useCase.currentWeekRange(now)
        assertEquals(at(2026, Calendar.NOVEMBER, 9), week.first)
        assertEquals(at(2026, Calendar.NOVEMBER, 16), week.last + 1)
        // El domingo pertenece a la semana que empieza el lunes anterior.
        val sunday = at(2026, Calendar.NOVEMBER, 15, 23)
        val sundayWeek = useCase.currentWeekRange(sunday)
        assertEquals(week.first, sundayWeek.first)
    }

    @Test
    fun `error de clientes se propaga como Result Error`() = runTest {
        val exception = Exception("DB Error")
        every { clientRepository.getAllClients() } returns flowOf(Result.Error(exception))
        every { jobRepository.getAllJobs() } returns flowOf(Result.Success(emptyList()))
        every { invoiceRepository.getAllInvoices() } returns flowOf(Result.Success(emptyList()))

        val result = useCase().first()

        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }
}
