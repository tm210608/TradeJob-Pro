package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.QuoteEntity
import com.tradejob.pro.database.data.entity.QuoteItemEntity
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getAllQuotes(): Flow<Result<List<QuoteEntity>>>
    fun getQuotesByClient(clientId: Long): Flow<Result<List<QuoteEntity>>>
    suspend fun getQuoteById(id: Long): Result<QuoteEntity?>
    suspend fun insertQuote(quote: QuoteEntity): Result<Long>
    suspend fun updateQuote(quote: QuoteEntity): Result<Unit>
    suspend fun deleteQuote(quote: QuoteEntity): Result<Unit>

    fun getItemsByQuote(quoteId: Long): Flow<Result<List<QuoteItemEntity>>>
    suspend fun getItemsByQuoteSync(quoteId: Long): Result<List<QuoteItemEntity>>
    suspend fun insertItem(item: QuoteItemEntity): Result<Long>
    suspend fun updateItem(item: QuoteItemEntity): Result<Unit>
    suspend fun deleteItem(item: QuoteItemEntity): Result<Unit>
    suspend fun deleteItemsByQuote(quoteId: Long): Result<Unit>
}
