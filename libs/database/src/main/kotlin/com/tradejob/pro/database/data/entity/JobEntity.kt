package com.tradejob.pro.database.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "jobs",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val title: String,
    val description: String? = null,
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED, INVOICED
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val createdAt: Long = System.currentTimeMillis(),
    val scheduledAt: Long? = null,
    val completedAt: Long? = null,
    val budgetAmount: Double? = null,
    val finalAmount: Double? = null,
    val isSynced: Boolean = false
)
