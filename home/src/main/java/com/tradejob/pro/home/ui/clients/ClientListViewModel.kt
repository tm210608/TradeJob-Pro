package com.tradejob.pro.home.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.home.domain.DeleteClientUseCase
import com.tradejob.pro.home.domain.GetClientsUseCase
import com.tradejob.pro.home.domain.SearchClientsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ClientListViewModel @Inject constructor(
    private val getClientsUseCase: GetClientsUseCase,
    private val searchClientsUseCase: SearchClientsUseCase,
    private val deleteClientUseCase: DeleteClientUseCase
) : ViewModel() {

    private val _clients = MutableStateFlow<List<ClientEntity>>(emptyList())
    val clients: StateFlow<List<ClientEntity>> = _clients

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableStateFlow<ClientListEvent>(ClientListEvent.None)
    val event: StateFlow<ClientListEvent> = _event

    init {
        observeClients()
    }

    private fun observeClients() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        getClientsUseCase()
                    } else {
                        searchClientsUseCase(query)
                    }
                }
                .collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            _clients.value = result.data
                            _isLoading.value = false
                        }
                        is Result.Error -> {
                            _event.value = ClientListEvent.Error("Error al cargar clientes")
                            _isLoading.value = false
                        }
                        is Result.Loading -> {
                            _isLoading.value = true
                        }
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            when (val result = deleteClientUseCase(client)) {
                is Result.Success -> {
                    _event.value = ClientListEvent.Success("Cliente eliminado")
                }
                is Result.Error -> {
                    _event.value = ClientListEvent.Error("Error al eliminar cliente")
                }
                else -> {}
            }
        }
    }

    fun clearEvent() {
        _event.value = ClientListEvent.None
    }
}

sealed class ClientListEvent {
    data object None : ClientListEvent()
    data class Success(val message: String) : ClientListEvent()
    data class Error(val message: String) : ClientListEvent()
}
