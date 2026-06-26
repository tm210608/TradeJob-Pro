package com.tradejob.pro.database.data.dao

import androidx.room.*
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobPhotoDao {
    @Query("SELECT * FROM job_photos WHERE jobId = :jobId ORDER BY createdAt ASC")
    fun getPhotosByJob(jobId: Long): Flow<List<JobPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: JobPhotoEntity): Long

    @Update
    suspend fun update(photo: JobPhotoEntity)

    @Delete
    suspend fun delete(photo: JobPhotoEntity)

    @Query("SELECT * FROM job_photos WHERE isSynced = 0")
    suspend fun getUnsyncedPhotos(): List<JobPhotoEntity>

    @Query("UPDATE job_photos SET isSynced = 1, remoteUrl = :remoteUrl WHERE id = :id")
    suspend fun markPhotoAsSynced(id: Long, remoteUrl: String?)
}
