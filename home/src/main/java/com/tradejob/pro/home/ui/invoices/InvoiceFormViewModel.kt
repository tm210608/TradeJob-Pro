package com.tradejob.pro.home.ui.invoices

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.common.analytics.AnalyticsHelper
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.InvoiceEntity
import com.tradejob.pro.database.data.entity.UserEntity
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.DeleteInvoiceUseCase
import com.tradejob.pro.home.domain.GenerateInvoicePdfUseCase
import com.tradejob.pro.home.domain.GetInvoiceByIdUseCase
import com.tradejob.pro.home.domain.SaveInvoiceUseCase
import com.tradejob.pro.home.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class InvoiceFormViewModel @Inject constructor(
    private val getInvoiceByIdUseCase: GetInvoiceByIdUseCase,
    private val saveInvoiceUseCase: SaveInvoiceUseCase,
    private val deleteInvoiceUseCase: DeleteInvoiceUseCase,
    private val generateInvoicePdfUseCase: GenerateInvoicePdfUseCase,
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository,
    private val analyticsHelper: AnalyticsHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val invoiceId: Long = savedStateHandle.get<String>("invoiceId")?.toLongOrNull() ?: 0L

    private val _invoice = MutableStateFlow<InvoiceEntity?>(null)
    val invoice: StateFlow<InvoiceEntity?> = _invoice

    private val _client = MutableStateFlow<ClientEntity?>(null)
    val client: StateFlow<ClientEntity?> = _client

    private val _status = MutableStateFlow(InvoiceStatus.PENDING)
    val status: StateFlow<InvoiceStatus> = _status

    private val _paymentMethod = MutableStateFlow<String?>(null)
    val paymentMethod: StateFlow<String?> = _paymentMethod

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableStateFlow<InvoiceFormEvent>(InvoiceFormEvent.None)
    val event: StateFlow<InvoiceFormEvent> = _event

    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile: StateFlow<File?> = _pdfFile

    private var professional: UserEntity? = null

    init {
        loadProfessional()
        if (invoiceId != 0L) loadInvoice(invoiceId)
    }

    private fun loadProfessional() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { user ->
                professional = user
            }
        }
    }

    private fun loadInvoice(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getInvoiceByIdUseCase(id)) {
                is Result.Success -> {
                    result.data?.let { invoice ->
                        _invoice.value = invoice
                        _status.value = InvoiceStatus.fromValue(invoice.status)
                        _paymentMethod.value = invoice.paymentMethod
                        _notes.value = invoice.notes.orEmpty()
                    }
                    loadClient(result.data?.clientId ?: 0L)
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _event.value = InvoiceFormEvent.Error("Error al cargar factura")
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }

    private fun loadClient(clientId: Long) {
        viewModelScope.launch {
            when (val result = clientRepository.getClientById(clientId)) {
                is Result.Success -> _client.value = result.data
                else -> {}
            }
        }
    }

    fun onStatusChanged(value: InvoiceStatus) {
        _status.value = value
    }

    fun onPaymentMethodChanged(value: String?) {
        _paymentMethod.value = value
    }

    fun onNotesChanged(value: String) {
        _notes.value = value
    }

    fun markAsPaid() {
        val current = _invoice.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updated = current.copy(
                status = InvoiceStatus.PAID.value,
                paidAmount = current.total,
                paidAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                paymentMethod = _paymentMethod.value ?: current.paymentMethod,
                notes = _notes.value.trim().ifBlank { null }
            )
            when (val res = saveInvoiceUseCase(updated, isNew = false)) {
                is Result.Success -> {
                    analyticsHelper.logEvent("invoice_marked_paid", mapOf("invoiceId" to updated.id.toString()))
                    _invoice.value = updated
                    _status.value = InvoiceStatus.PAID
                    _event.value = InvoiceFormEvent.Success("Factura marcada como pagada")
                    _isLoading.value = false
                }
                is Result.Error -> {
                    analyticsHelper.logError(res.exception, "Failed to mark invoice ${current.id} as paid")
                    _event.value = InvoiceFormEvent.Error("Error al actualizar factura")
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }

    fun save() {
        val current = _invoice.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updated = current.copy(
                status = _status.value.value,
                paymentMethod = _paymentMethod.value,
                notes = _notes.value.trim().ifBlank { null },
                updatedAt = System.currentTimeMillis()
            )
            when (saveInvoiceUseCase(updated, isNew = false)) {
                is Result.Success -> {
                    _event.value = InvoiceFormEvent.Success("Factura actualizada")
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _event.value = InvoiceFormEvent.Error("Error al guardar factura")
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }

    fun delete() {
        val current = _invoice.value ?: return
        viewModelScope.launch {
            when (deleteInvoiceUseCase(current)) {
                is Result.Success -> _event.value = InvoiceFormEvent.Success("Factura eliminada")
                is Result.Error -> _event.value = InvoiceFormEvent.Error("Error al eliminar factura")
                else -> {}
            }
        }
    }

    fun exportPdf(context: Context) {
        val invoice = _invoice.value ?: return
        val client = _client.value ?: return
        viewModelScope.launch {
            val file = generateInvoicePdfUseCase(context, client, invoice, professional)
            if (file != null) {
                analyticsHelper.logEvent("invoice_pdf_exported", mapOf("invoiceId" to invoice.id.toString()))
                _pdfFile.value = file
                _event.value = InvoiceFormEvent.Success("PDF generado correctamente")
            } else {
                _event.value = InvoiceFormEvent.Error("Error al generar PDF")
            }
        }
    }

    fun clearEvent() {
        _event.value = InvoiceFormEvent.None
    }
}

sealed class InvoiceFormEvent {
    data object None : InvoiceFormEvent()
    data class Success(val message: String) : InvoiceFormEvent()
    data class Error(val message: String) : InvoiceFormEvent()
}
