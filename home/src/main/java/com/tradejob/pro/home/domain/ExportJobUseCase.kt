package com.tradejob.pro.home.domain

import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.ui.jobs.JobStatus
import javax.inject.Inject

class ExportJobUseCase @Inject constructor() {
    operator fun invoke(job: JobEntity): String {
        val status = JobStatus.fromValue(job.status).displayName
        val priority = job.priority
        val sb = StringBuilder()
        sb.append("🛠 *Reporte de Trabajo - TradeJob Pro*\n\n")
        sb.append("📌 *Título:* ${job.title}\n")
        sb.append("📊 *Estado:* $status\n")
        sb.append("⚡ *Prioridad:* $priority\n")
        
        job.budgetAmount?.let {
            sb.append("💰 *Presupuesto:* $it€\n")
        }

        if (!job.description.isNullOrBlank()) {
            sb.append("📝 *Descripción:* ${job.description}\n")
        }
        
        sb.append("\n_Generado desde TradeJob Pro_")
        return sb.toString()
    }
}
