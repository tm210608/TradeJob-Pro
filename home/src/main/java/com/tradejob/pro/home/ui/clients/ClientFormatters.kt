package com.tradejob.pro.home.ui.clients

import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.ui.jobs.JobStatus

// Conteos de la línea de resumen de la ficha. "Terminados" = COMPLETED + INVOICED,
// la misma regla que usa el dashboard (SPEC-011).
data class JobStatusCounts(
    val inProgress: Int,
    val pending: Int,
    val completed: Int
)

fun countJobsByStatus(jobs: List<JobEntity>): JobStatusCounts = JobStatusCounts(
    inProgress = jobs.count { it.status == JobStatus.IN_PROGRESS.value },
    pending = jobs.count { it.status == JobStatus.PENDING.value },
    completed = jobs.count {
        it.status == JobStatus.COMPLETED.value || it.status == JobStatus.INVOICED.value
    }
)

// "Último trabajo" = el creado más recientemente (decisión de la spec).
fun lastJob(jobs: List<JobEntity>): JobEntity? = jobs.maxByOrNull { it.createdAt }
