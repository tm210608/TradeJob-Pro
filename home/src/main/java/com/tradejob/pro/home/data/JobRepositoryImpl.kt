package com.tradejob.pro.home.data

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.dao.JobDao
import com.tradejob.pro.database.data.dao.JobPhotoDao
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import com.tradejob.pro.home.domain.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val jobPhotoDao: JobPhotoDao
) : JobRepository {

    override fun getAllJobs(): Flow<Result<List<JobEntity>>> = flow {
        emit(Result.Loading)
        try {
            jobDao.getAllJobs().collect { jobs ->
                emit(Result.Success(jobs))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override fun getJobsByClient(clientId: Long): Flow<Result<List<JobEntity>>> = flow {
        emit(Result.Loading)
        try {
            jobDao.getJobsByClient(clientId).collect { jobs ->
                emit(Result.Success(jobs))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun getJobById(id: Long): Result<JobEntity?> {
        return try {
            Result.Success(jobDao.getJobById(id))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertJob(job: JobEntity): Result<Long> {
        return try {
            val id = jobDao.insert(job)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateJob(job: JobEntity): Result<Unit> {
        return try {
            jobDao.update(job)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteJob(job: JobEntity): Result<Unit> {
        return try {
            jobDao.delete(job)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getPhotosByJob(jobId: Long): Flow<Result<List<JobPhotoEntity>>> = flow {
        emit(Result.Loading)
        try {
            jobPhotoDao.getPhotosByJob(jobId).collect { photos ->
                emit(Result.Success(photos))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun insertPhoto(photo: JobPhotoEntity): Result<Long> {
        return try {
            val id = jobPhotoDao.insert(photo)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deletePhoto(photo: JobPhotoEntity): Result<Unit> {
        return try {
            jobPhotoDao.delete(photo)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
