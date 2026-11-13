package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.ui.invoices.InvoiceStatus
import com.tradejob.pro.home.ui.jobs.JobStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

data class HomeStats(
    val totalClients: Int,
    val pendingJobs: Int,
    val inProgressJobs: Int,
    // "Terminados" = COMPLETED + INVOICED (SPEC-011): los facturados también están hechos.
    val completedJobs: Int,
    val totalJobs: Int,
    val jobsThisWeek: Int,
    val completedThisWeek: Int,
    val paidInvoicesThisWeek: Int,
    val recentJobs: List<JobEntity> = emptyList()
) {
    val isEmpty: Boolean get() = totalClients == 0 && totalJobs == 0
    val hasCelebration: Boolean get() = completedThisWeek > 0 || paidInvoicesThisWeek > 0
}

class GetHomeStatsUseCase @Inject constructor(
    private val clientRepository: ClientRepository,
    private val jobRepository: JobRepository,
    private val invoiceRepository: InvoiceRepository
) {
    // Reloj inyectable solo para tests JVM (SPEC-011).
    internal var nowProvider: () -> Long = { System.currentTimeMillis() }

    operator fun invoke(): Flow<Result<HomeStats>> = combine(
        clientRepository.getAllClients(),
        jobRepository.getAllJobs(),
        invoiceRepository.getAllInvoices()
    ) { clientsResult, jobsResult, invoicesResult ->
        if (clientsResult is Result.Success && jobsResult is Result.Success && invoicesResult is Result.Success) {
            val jobs = jobsResult.data
            val week = currentWeekRange(nowProvider())
            Result.Success(
                HomeStats(
                    totalClients = clientsResult.data.size,
                    pendingJobs = jobs.count { it.status == JobStatus.PENDING.value },
                    inProgressJobs = jobs.count { it.status == JobStatus.IN_PROGRESS.value },
                    completedJobs = jobs.count {
                        it.status == JobStatus.COMPLETED.value || it.status == JobStatus.INVOICED.value
                    },
                    totalJobs = jobs.size,
                    jobsThisWeek = jobs.count { (it.scheduledAt ?: it.createdAt) in week },
                    completedThisWeek = jobs.count { it.completedAt?.let { t -> t in week } == true },
                    paidInvoicesThisWeek = invoicesResult.data.count {
                        it.status == InvoiceStatus.PAID.value && it.paidAt?.let { t -> t in week } == true
                    },
                    recentJobs = jobs.take(3)
                )
            )
        } else if (clientsResult is Result.Error) {
            Result.Error(clientsResult.exception)
        } else if (jobsResult is Result.Error) {
            Result.Error(jobsResult.exception)
        } else if (invoicesResult is Result.Error) {
            Result.Error(invoicesResult.exception)
        } else {
            Result.Loading
        }
    }

    // Semana en curso lunes 00:00 – lunes siguiente 00:00 (hora local), fin exclusivo.
    // DAY_OF_WEEK es fijo (domingo=1…sábado=7): el lunes no depende de la locale.
    internal fun currentWeekRange(now: Long): LongRange {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 7)
        return start until cal.timeInMillis
    }
}
