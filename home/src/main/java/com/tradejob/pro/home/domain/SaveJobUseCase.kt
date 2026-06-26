package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import javax.inject.Inject

class SaveJobUseCase @Inject constructor(
    private val repository: JobRepository
) {
    suspend operator fun invoke(job: JobEntity, isNew: Boolean): Result<Long> {
        return if (isNew) {
            repository.insertJob(job)
        } else {
            val result = repository.updateJob(job)
            if (result is Result.Success) Result.Success(job.id) else result as Result.Error
        }
    }
}
