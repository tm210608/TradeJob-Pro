package com.tradejob.pro.database.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "job_photos",
    foreignKeys = [
        ForeignKey(
            entity = JobEntity::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["jobId"])]
)
data class JobPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val photoPath: String,
    val description: String? = null,
    val type: String = "BEFORE", // BEFORE, AFTER, PROGRESS
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val remoteUrl: String? = null
)
