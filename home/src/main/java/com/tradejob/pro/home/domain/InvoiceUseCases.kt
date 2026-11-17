package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.InvoiceEntity
import com.tradejob.pro.database.data.entity.QuoteEntity
import com.tradejob.pro.database.data.entity.QuoteItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllInvoicesUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    operator fun invoke(): Flow<Result<List<InvoiceEntity>>> = repository.getAllInvoices()
}

class GetInvoicesByClientUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    operator fun invoke(clientId: Long): Flow<Result<List<InvoiceEntity>>> = repository.getInvoicesByClient(clientId)
}

class GetInvoiceByIdUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(id: Long): Result<InvoiceEntity?> = repository.getInvoiceById(id)
}

class SaveInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(invoice: InvoiceEntity, isNew: Boolean): Result<Long> {
        return if (isNew) repository.insertInvoice(invoice)
        else {
            val result = repository.updateInvoice(invoice)
            if (result is Result.Success) Result.Success(invoice.id) else result as Result.Error
        }
    }
}

class DeleteInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(invoice: InvoiceEntity): Result<Unit> = repository.deleteInvoice(invoice)
}

class NextInvoiceNumberUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val numberStore: DocumentNumberStore
) {
    suspend operator fun invoke(): Result<String> {
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return when (val result = repository.getInvoiceNumbersLike(DocumentNumber.likePattern("F", year))) {
            is Result.Success -> {
                val sequence = DocumentNumber.nextSequence("F", year, result.data, numberStore.lastIssued("F", year))
                numberStore.recordIssued("F", year, sequence)
                Result.Success(DocumentNumber.format("F", year, sequence))
            }
            is Result.Error -> result
            else -> Result.Error(IllegalStateException("Estado inesperado"))
        }
    }
}

class CreateInvoiceFromQuoteUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val quoteRepository: QuoteRepository,
    private val nextInvoiceNumberUseCase: NextInvoiceNumberUseCase
) {
    suspend operator fun invoke(quote: QuoteEntity): Result<Long> {
        val itemsResult = quoteRepository.getItemsByQuoteSync(quote.id)
        if (itemsResult is Result.Error) return itemsResult as Result.Error

        val items = (itemsResult as Result.Success).data
        val subtotal = items.sumOf { it.total }
        val vatAmount = subtotal * quote.vatRate / 100.0
        val total = subtotal + vatAmount

        val number = when (val numberResult = nextInvoiceNumberUseCase()) {
            is Result.Success -> numberResult.data
            else -> "F-${System.currentTimeMillis() / 1000}"
        }

        val invoice = InvoiceEntity(
            clientId = quote.clientId,
            quoteId = quote.id,
            number = number,
            status = "PENDING",
            vatRate = quote.vatRate,
            subtotal = subtotal,
            vatAmount = vatAmount,
            total = total,
            notes = quote.notes
        )
        return invoiceRepository.insertInvoice(invoice)
    }
}
