package com.tradejob.pro.database.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "invoices",
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
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val quoteId: Long? = null,
    val jobId: Long? = null,
    val number: String = "",
    val status: String = "PENDING", // PENDING, PAID, OVERDUE, CANCELLED
    val paymentMethod: String? = null, // CASH, CARD, TRANSFER, BIZUM
    val vatRate: Double = 21.0,
    val subtotal: Double = 0.0,
    val vatAmount: Double = 0.0,
    val irpfRate: Double? = null,
    val total: Double = 0.0,
    val paidAmount: Double = 0.0,
    val issuedAt: Long = System.currentTimeMillis(),
    val dueAt: Long? = null,
    val paidAt: Long? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
