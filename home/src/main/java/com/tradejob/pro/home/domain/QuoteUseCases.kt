package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.QuoteEntity
import com.tradejob.pro.database.data.entity.QuoteItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuotesByClientUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    operator fun invoke(clientId: Long): Flow<Result<List<QuoteEntity>>> {
        return repository.getQuotesByClient(clientId)
    }
}

class GetQuoteByIdUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(id: Long): Result<QuoteEntity?> {
        return repository.getQuoteById(id)
    }
}

class SaveQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(quote: QuoteEntity, items: List<QuoteItemEntity>, isNew: Boolean): Result<Long> {
        val result = if (isNew) {
            repository.insertQuote(quote)
        } else {
            val updateResult = repository.updateQuote(quote)
            if (updateResult is Result.Success) Result.Success(quote.id) else updateResult as Result.Error
        }

        if (result is Result.Success) {
            val quoteId = result.data
            if (isNew) {
                items.forEachIndexed { index, item ->
                    repository.insertItem(item.copy(quoteId = quoteId, sortOrder = index))
                }
            } else {
                repository.deleteItemsByQuote(quoteId)
                items.forEachIndexed { index, item ->
                    repository.insertItem(item.copy(quoteId = quoteId, sortOrder = index, id = 0))
                }
            }
        }

        return result
    }
}

class DeleteQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(quote: QuoteEntity): Result<Unit> {
        return repository.deleteQuote(quote)
    }
}

class GetQuoteItemsUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    operator fun invoke(quoteId: Long): Flow<Result<List<QuoteItemEntity>>> {
        return repository.getItemsByQuote(quoteId)
    }
}

class GetQuoteItemsSyncUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(quoteId: Long): Result<List<QuoteItemEntity>> {
        return repository.getItemsByQuoteSync(quoteId)
    }
}
