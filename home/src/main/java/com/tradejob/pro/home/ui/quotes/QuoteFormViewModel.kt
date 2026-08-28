package com.tradejob.pro.home.ui.quotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.tradejob.pro.common.analytics.AnalyticsHelper
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.QuoteEntity
import com.tradejob.pro.database.data.entity.QuoteItemEntity
import com.tradejob.pro.database.data.entity.UserEntity
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.CreateInvoiceFromQuoteUseCase
import com.tradejob.pro.home.domain.DeleteQuoteUseCase
import com.tradejob.pro.home.domain.GenerateQuotePdfUseCase
import com.tradejob.pro.home.domain.GetQuoteByIdUseCase
import com.tradejob.pro.home.domain.GetQuoteItemsSyncUseCase
import com.tradejob.pro.home.domain.SaveQuoteUseCase
import com.tradejob.pro.home.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class QuoteFormViewModel @Inject constructor(
    private val saveQuoteUseCase: SaveQuoteUseCase,
    private val getQuoteByIdUseCase: GetQuoteByIdUseCase,
    private val getQuoteItemsSyncUseCase: GetQuoteItemsSyncUseCase,
    private val deleteQuoteUseCase: DeleteQuoteUseCase,
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository,
    private val generateQuotePdfUseCase: GenerateQuotePdfUseCase,
    private val createInvoiceFromQuoteUseCase: CreateInvoiceFromQuoteUseCase,
    private val analyticsHelper: AnalyticsHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clientId: Long = savedStateHandle.get<String>("clientId")?.toLongOrNull() ?: 0L
    private val quoteId: Long? = savedStateHandle.get<String>("quoteId")?.toLongOrNull()

    private val _items = MutableStateFlow<List<QuoteItemEntity>>(emptyList())
    val items: StateFlow<List<QuoteItemEntity>> = _items

    private val _vatRate = MutableStateFlow(21.0)
    val vatRate: StateFlow<Double> = _vatRate

    private val _status = MutableStateFlow(QuoteStatus.DRAFT)
    val status: StateFlow<QuoteStatus> = _status

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableStateFlow<QuoteFormEvent>(QuoteFormEvent.None)
    val event: StateFlow<QuoteFormEvent> = _event

    private var loadedQuote: QuoteEntity? = null
    private var loadedClient: ClientEntity? = null
    private var loadedProfessional: UserEntity? = null

    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile: StateFlow<File?> = _pdfFile

    private val _isConverting = MutableStateFlow(false)
    val isConverting: StateFlow<Boolean> = _isConverting

    val isEditMode: Boolean get() = quoteId != null && quoteId != 0L

    init {
        loadClient()
        loadProfessional()
        if (isEditMode) {
            quoteId?.let { loadQuote(it) }
        }
    }

    private fun loadClient() {
        viewModelScope.launch {
            when (val result = clientRepository.getClientById(clientId)) {
                is Result.Success -> loadedClient = result.data
                else -> {}
            }
        }
    }

    private fun loadProfessional() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { user ->
                loadedProfessional = user
            }
        }
    }

    private fun loadQuote(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getQuoteByIdUseCase(id)) {
                is Result.Success -> {
                    result.data?.let { quote ->
                        loadedQuote = quote
                        _vatRate.value = quote.vatRate
                        _status.value = QuoteStatus.fromValue(quote.status)
                        _notes.value = quote.notes.orEmpty()
                    }
                    when (val itemsResult = getQuoteItemsSyncUseCase(id)) {
                        is Result.Success -> _items.value = itemsResult.data
                        else -> {}
                    }
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _event.value = QuoteFormEvent.Error("Error al cargar presupuesto")
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }

    fun addItem(description: String, quantity: Double, unitPrice: Double) {
        val total = quantity * unitPrice
        val newItem = QuoteItemEntity(
            quoteId = quoteId ?: 0L,
            description = description,
            quantity = quantity,
            unitPrice = unitPrice,
            total = total
        )
        _items.value = _items.value + newItem
    }

    fun updateItem(index: Int, description: String, quantity: Double, unitPrice: Double) {
        val updated = _items.value.toMutableList()
        if (index in updated.indices) {
            updated[index] = updated[index].copy(
                description = description,
                quantity = quantity,
                unitPrice = unitPrice,
                total = quantity * unitPrice
            )
            _items.value = updated
        }
    }

    fun removeItem(index: Int) {
        val updated = _items.value.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _items.value = updated
        }
    }

    fun onVatRateChanged(value: Double) {
        _vatRate.value = value
    }

    fun onStatusChanged(value: QuoteStatus) {
        _status.value = value
    }

    fun onNotesChanged(value: String) {
        _notes.value = value
    }

    fun calculateSubtotal(): Double = _items.value.sumOf { it.total }
    fun calculateVatAmount(): Double = calculateSubtotal() * _vatRate.value / 100.0
    fun calculateTotal(): Double = calculateSubtotal() + calculateVatAmount()

    fun save() {
        if (_items.value.isEmpty()) {
            _event.value = QuoteFormEvent.Error("Añade al menos una partida al presupuesto")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val subtotal = calculateSubtotal()
            val vatAmount = calculateVatAmount()
            val total = calculateTotal()

            val quote = QuoteEntity(
                id = if (isEditMode) quoteId ?: 0L else 0L,
                clientId = clientId,
                number = loadedQuote?.number ?: "P-${System.currentTimeMillis() / 1000}",
                status = _status.value.value,
                vatRate = _vatRate.value,
                subtotal = subtotal,
                vatAmount = vatAmount,
                total = total,
                notes = _notes.value.trim().ifBlank { null },
                createdAt = loadedQuote?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            when (val result = saveQuoteUseCase(quote, _items.value, !isEditMode)) {
                is Result.Success -> {
                    _event.value = QuoteFormEvent.Success(
                        if (isEditMode) "Presupuesto actualizado" else "Presupuesto creado"
                    )
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _event.value = QuoteFormEvent.Error("Error al guardar presupuesto")
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }

    fun delete() {
        val current = loadedQuote ?: return
        viewModelScope.launch {
            when (deleteQuoteUseCase(current)) {
                is Result.Success -> _event.value = QuoteFormEvent.Success("Presupuesto eliminado")
                is Result.Error -> _event.value = QuoteFormEvent.Error("Error al eliminar presupuesto")
                else -> {}
            }
        }
    }

    fun exportPdf(context: Context, signaturePath: String? = null) {
        val quote = loadedQuote ?: return
        val client = loadedClient ?: return
        viewModelScope.launch {
            val items = if (_items.value.isNotEmpty()) _items.value else {
                when (val r = getQuoteItemsSyncUseCase(quote.id)) {
                    is Result.Success -> r.data
                    else -> emptyList()
                }
            }
            val file = generateQuotePdfUseCase(context, client, quote, items, loadedProfessional, signaturePath)
            if (file != null) {
                analyticsHelper.logEvent("quote_pdf_exported", mapOf("quoteId" to quote.id.toString()))
                _pdfFile.value = file
                _event.value = QuoteFormEvent.Success("PDF generado correctamente")
            } else {
                _event.value = QuoteFormEvent.Error("Error al generar PDF")
            }
        }
    }

    fun convertToInvoice() {
        val quote = loadedQuote ?: return
        if (quote.status != QuoteStatus.ACCEPTED.value) return
        viewModelScope.launch {
            _isConverting.value = true
            when (val result = createInvoiceFromQuoteUseCase(quote)) {
                is Result.Success -> {
                    analyticsHelper.logEvent("quote_converted_to_invoice", mapOf("quoteId" to quote.id.toString()))
                    _event.value = QuoteFormEvent.Success("Factura creada desde presupuesto")
                    _isConverting.value = false
                }
                is Result.Error -> {
                    analyticsHelper.logError(result.exception, "Failed to convert quote ${quote.id} to invoice")
                    _event.value = QuoteFormEvent.Error("Error al crear factura")
                    _isConverting.value = false
                }
                else -> {}
            }
        }
    }

    fun clearPdfFile() {
        _pdfFile.value = null
    }

    fun clearEvent() {
        _event.value = QuoteFormEvent.None
    }
}

sealed class QuoteFormEvent {
    data object None : QuoteFormEvent()
    data class Success(val message: String) : QuoteFormEvent()
    data class Error(val message: String) : QuoteFormEvent()
}
