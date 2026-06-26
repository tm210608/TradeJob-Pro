package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotosByJobUseCase @Inject constructor(
    private val repository: JobRepository
) {
    operator fun invoke(jobId: Long): Flow<Result<List<JobPhotoEntity>>> {
        return repository.getPhotosByJob(jobId)
    }
}
