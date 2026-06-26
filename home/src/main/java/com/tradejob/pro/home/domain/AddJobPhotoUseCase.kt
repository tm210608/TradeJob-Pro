package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import javax.inject.Inject

class AddJobPhotoUseCase @Inject constructor(
    private val repository: JobRepository
) {
    suspend operator fun invoke(photo: JobPhotoEntity): Result<Long> {
        return repository.insertPhoto(photo)
    }
}
