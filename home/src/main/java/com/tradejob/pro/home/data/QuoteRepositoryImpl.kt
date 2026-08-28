package com.tradejob.pro.home.data

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.dao.QuoteDao
import com.tradejob.pro.database.data.dao.QuoteItemDao
import com.tradejob.pro.database.data.entity.QuoteEntity
import com.tradejob.pro.database.data.entity.QuoteItemEntity
import com.tradejob.pro.home.domain.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class QuoteRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao,
    private val quoteItemDao: QuoteItemDao
) : QuoteRepository {

    override fun getAllQuotes(): Flow<Result<List<QuoteEntity>>> = flow {
        emit(Result.Loading)
        try {
            quoteDao.getAllQuotes().collect { quotes ->
                emit(Result.Success(quotes))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override fun getQuotesByClient(clientId: Long): Flow<Result<List<QuoteEntity>>> = flow {
        emit(Result.Loading)
        try {
            quoteDao.getQuotesByClient(clientId).collect { quotes ->
                emit(Result.Success(quotes))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun getQuoteById(id: Long): Result<QuoteEntity?> {
        return try {
            Result.Success(quoteDao.getQuoteById(id))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertQuote(quote: QuoteEntity): Result<Long> {
        return try {
            Result.Success(quoteDao.insert(quote))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateQuote(quote: QuoteEntity): Result<Unit> {
        return try {
            quoteDao.update(quote)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteQuote(quote: QuoteEntity): Result<Unit> {
        return try {
            quoteDao.delete(quote)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getItemsByQuote(quoteId: Long): Flow<Result<List<QuoteItemEntity>>> = flow {
        emit(Result.Loading)
        try {
            quoteItemDao.getItemsByQuote(quoteId).collect { items ->
                emit(Result.Success(items))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun getItemsByQuoteSync(quoteId: Long): Result<List<QuoteItemEntity>> {
        return try {
            Result.Success(quoteItemDao.getItemsByQuoteSync(quoteId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertItem(item: QuoteItemEntity): Result<Long> {
        return try {
            Result.Success(quoteItemDao.insert(item))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateItem(item: QuoteItemEntity): Result<Unit> {
        return try {
            quoteItemDao.update(item)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteItem(item: QuoteItemEntity): Result<Unit> {
        return try {
            quoteItemDao.delete(item)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteItemsByQuote(quoteId: Long): Result<Unit> {
        return try {
            quoteItemDao.deleteByQuote(quoteId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
