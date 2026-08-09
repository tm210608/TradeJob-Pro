package com.tradejob.pro.database.data.dao

import androidx.room.*
import com.tradejob.pro.database.data.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issuedAt DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE clientId = :clientId ORDER BY issuedAt DESC")
    fun getInvoicesByClient(clientId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY issuedAt DESC")
    fun getInvoicesByStatus(status: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceEntity): Long

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Delete
    suspend fun delete(invoice: InvoiceEntity)

    @Query("SELECT * FROM invoices WHERE isSynced = 0")
    suspend fun getUnsyncedInvoices(): List<InvoiceEntity>

    @Query("UPDATE invoices SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markInvoicesAsSynced(ids: List<Long>)
}
