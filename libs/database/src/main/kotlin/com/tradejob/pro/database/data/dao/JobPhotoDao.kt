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
}
