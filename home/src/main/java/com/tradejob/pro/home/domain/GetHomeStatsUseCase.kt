package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.ui.jobs.JobStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class HomeStats(
    val totalClients: Int,
    val pendingJobs: Int,
    val inProgressJobs: Int,
    val completedJobs: Int,
    val recentJobs: List<JobEntity> = emptyList()
)

class GetHomeStatsUseCase @Inject constructor(
    private val clientRepository: ClientRepository,
    private val jobRepository: JobRepository
) {
    operator fun invoke(): Flow<Result<HomeStats>> = combine(
        clientRepository.getAllClients(),
        jobRepository.getAllJobs()
    ) { clientsResult, jobsResult ->
        if (clientsResult is Result.Success && jobsResult is Result.Success) {
            val jobs = jobsResult.data
            Result.Success(
                HomeStats(
                    totalClients = clientsResult.data.size,
                    pendingJobs = jobs.count { it.status == JobStatus.PENDING.value },
                    inProgressJobs = jobs.count { it.status == JobStatus.IN_PROGRESS.value },
                    completedJobs = jobs.count { it.status == JobStatus.COMPLETED.value },
                    recentJobs = jobs.take(3)
                )
            )
        } else if (clientsResult is Result.Error) {
            Result.Error(clientsResult.exception)
        } else if (jobsResult is Result.Error) {
            Result.Error(jobsResult.exception)
        } else {
            Result.Loading
        }
    }
}
