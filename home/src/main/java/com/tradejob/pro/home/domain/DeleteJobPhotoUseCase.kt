package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import javax.inject.Inject

class DeleteJobPhotoUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    suspend operator fun invoke(photo: JobPhotoEntity): Result<Unit> {
        return jobRepository.deletePhoto(photo)
    }
}
