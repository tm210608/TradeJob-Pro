package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun getAllInvoices(): Flow<Result<List<InvoiceEntity>>>
    fun getInvoicesByClient(clientId: Long): Flow<Result<List<InvoiceEntity>>>
    fun getInvoicesByStatus(status: String): Flow<Result<List<InvoiceEntity>>>
    suspend fun getInvoiceById(id: Long): Result<InvoiceEntity?>
    suspend fun insertInvoice(invoice: InvoiceEntity): Result<Long>
    suspend fun updateInvoice(invoice: InvoiceEntity): Result<Unit>
    suspend fun deleteInvoice(invoice: InvoiceEntity): Result<Unit>
}
