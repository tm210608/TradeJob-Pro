package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJobsByClientUseCase @Inject constructor(
    private val repository: JobRepository
) {
    operator fun invoke(clientId: Long): Flow<Result<List<JobEntity>>> {
        return repository.getJobsByClient(clientId)
    }
}
