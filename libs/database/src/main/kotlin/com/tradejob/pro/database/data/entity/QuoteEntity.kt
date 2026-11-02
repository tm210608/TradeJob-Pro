package com.tradejob.pro.database.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "quotes",
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
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val jobId: Long? = null,
    val number: String = "",
    val status: String = "DRAFT", // DRAFT, SENT, ACCEPTED, REJECTED
    val vatRate: Double = 21.0,
    val subtotal: Double = 0.0,
    val vatAmount: Double = 0.0,
    val total: Double = 0.0,
    val notes: String? = null,
    val validUntil: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
