package com.tradejob.pro.home.data

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.dao.InvoiceDao
import com.tradejob.pro.database.data.entity.InvoiceEntity
import com.tradejob.pro.home.domain.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {

    override fun getAllInvoices(): Flow<Result<List<InvoiceEntity>>> = flow {
        emit(Result.Loading)
        try {
            invoiceDao.getAllInvoices().collect { emit(Result.Success(it)) }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override fun getInvoicesByClient(clientId: Long): Flow<Result<List<InvoiceEntity>>> = flow {
        emit(Result.Loading)
        try {
            invoiceDao.getInvoicesByClient(clientId).collect { emit(Result.Success(it)) }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override fun getInvoicesByStatus(status: String): Flow<Result<List<InvoiceEntity>>> = flow {
        emit(Result.Loading)
        try {
            invoiceDao.getInvoicesByStatus(status).collect { emit(Result.Success(it)) }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun getInvoiceById(id: Long): Result<InvoiceEntity?> {
        return try { Result.Success(invoiceDao.getInvoiceById(id)) } catch (e: Exception) { Result.Error(e) }
    }

    override suspend fun insertInvoice(invoice: InvoiceEntity): Result<Long> {
        return try { Result.Success(invoiceDao.insert(invoice)) } catch (e: Exception) { Result.Error(e) }
    }

    override suspend fun updateInvoice(invoice: InvoiceEntity): Result<Unit> {
        return try { invoiceDao.update(invoice); Result.Success(Unit) } catch (e: Exception) { Result.Error(e) }
    }

    override suspend fun deleteInvoice(invoice: InvoiceEntity): Result<Unit> {
        return try { invoiceDao.delete(invoice); Result.Success(Unit) } catch (e: Exception) { Result.Error(e) }
    }
}
