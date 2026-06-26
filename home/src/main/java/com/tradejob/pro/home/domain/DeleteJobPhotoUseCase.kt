package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.dao.JobPhotoDao
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import javax.inject.Inject

class DeleteJobPhotoUseCase @Inject constructor(
    private val jobPhotoDao: JobPhotoDao
) {
    suspend operator fun invoke(photo: JobPhotoEntity): Result<Unit> {
        return try {
            jobPhotoDao.delete(photo)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
