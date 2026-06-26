package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import javax.inject.Inject

class GetJobByIdUseCase @Inject constructor(
    private val repository: JobRepository
) {
    suspend operator fun invoke(id: Long): Result<JobEntity?> {
        return repository.getJobById(id)
    }
}
