package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import javax.inject.Inject

class DeleteJobUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    suspend operator fun invoke(job: JobEntity): Result<Unit> {
        return jobRepository.deleteJob(job)
    }
}
