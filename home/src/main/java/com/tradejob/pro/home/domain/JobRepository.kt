package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun getAllJobs(): Flow<Result<List<JobEntity>>>
    fun getJobsByClient(clientId: Long): Flow<Result<List<JobEntity>>>
    suspend fun getJobById(id: Long): Result<JobEntity?>
    suspend fun insertJob(job: JobEntity): Result<Long>
    suspend fun updateJob(job: JobEntity): Result<Unit>
    suspend fun deleteJob(job: JobEntity): Result<Unit>
    
    // Photos
    fun getPhotosByJob(jobId: Long): Flow<Result<List<JobPhotoEntity>>>
    suspend fun insertPhoto(photo: JobPhotoEntity): Result<Long>
    suspend fun deletePhoto(photo: JobPhotoEntity): Result<Unit>
}
