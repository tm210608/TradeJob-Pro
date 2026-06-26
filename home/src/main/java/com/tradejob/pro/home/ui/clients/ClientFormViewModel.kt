package com.tradejob.pro.home.ui.clients

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.SaveClientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientFormViewModel @Inject constructor(
    private val saveClientUseCase: SaveClientUseCase,
    private val clientRepository: ClientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clientId: Long? = savedStateHandle.get<String>("clientId")?.toLongOrNull()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isValid = MutableStateFlow(false)
    val isValid: StateFlow<Boolean> = _isValid

    private val _event = MutableStateFlow<ClientFormEvent>(ClientFormEvent.None)
    val event: StateFlow<ClientFormEvent> = _event

    private var loadedClient: ClientEntity? = null

    val isEditMode: Boolean get() = clientId != null && clientId != 0L

    init {
        if (isEditMode) {
            clientId?.let { loadClient(it) }
        }
    }

    private fun loadClient(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = clientRepository.getClientById(id)) {
                is Result.Success -> {
                    result.data?.let { client ->
                        loadedClient = client
                        _name.value = client.name
                        _phone.value = client.phone
                        _email.value = client.email.orEmpty()
                        _address.value = client.address.orEmpty()
                        _notes.value = client.notes.orEmpty()
                    }
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _event.value = ClientFormEvent.Error("Error al cargar cliente")
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }

    fun onNameChanged(value: String) {
        _name.value = value
        validate()
    }

    fun onPhoneChanged(value: String) {
        _phone.value = value
        validate()
    }

    fun onEmailChanged(value: String) {
        _email.value = value
        _emailError.value = if (value.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            "El formato del correo electrónico no es válido (ej: usuario@empresa.com)"
        } else {
            null
        }
        validate()
    }

    fun onAddressChanged(value: String) {
        _address.value = value
        validate()
    }

    fun onNotesChanged(value: String) {
        _notes.value = value
        validate()
    }

    private fun validate() {
        _isValid.value = _name.value.isNotBlank() && 
                _phone.value.isNotBlank() && 
                _emailError.value == null
    }

    fun save() {
        if (!_isValid.value) {
            _event.value = ClientFormEvent.Error("Nombre y teléfono son obligatorios")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val client = ClientEntity(
                id = if (isEditMode) clientId ?: 0L else 0L,
                name = _name.value.trim(),
                phone = _phone.value.trim(),
                email = _email.value.trim(),
                address = _address.value.trim(),
                notes = _notes.value.trim(),
                createdAt = loadedClient?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            saveClientUseCase(client, !isEditMode).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _event.value = ClientFormEvent.Success(
                            if (isEditMode) "Cliente actualizado" else "Cliente creado"
                        )
                        _isLoading.value = false
                    }
                    is Result.Error -> {
                        _event.value = ClientFormEvent.Error("Error al guardar cliente")
                        _isLoading.value = false
                    }
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }

    fun clearEvent() {
        _event.value = ClientFormEvent.None
    }
}

sealed class ClientFormEvent {
    data object None : ClientFormEvent()
    data class Success(val message: String) : ClientFormEvent()
    data class Error(val message: String) : ClientFormEvent()
}
