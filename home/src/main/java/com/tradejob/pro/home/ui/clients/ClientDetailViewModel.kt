package com.tradejob.pro.home.ui.clients

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.GetJobsByClientUseCase
import com.tradejob.pro.home.ui.jobs.JobStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val getJobsByClientUseCase: GetJobsByClientUseCase,
    private val deleteJobUseCase: com.tradejob.pro.home.domain.DeleteJobUseCase,
    private val exportJobUseCase: com.tradejob.pro.home.domain.ExportJobUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clientId: Long = savedStateHandle.get<String>("clientId")?.toLongOrNull() ?: 0L

    private val _client = MutableStateFlow<ClientEntity?>(null)
    val client: StateFlow<ClientEntity?> = _client

    private val _allJobs = MutableStateFlow<List<JobEntity>>(emptyList())
    
    private val _selectedFilter = MutableStateFlow<JobStatus?>(null)
    val selectedFilter: StateFlow<JobStatus?> = _selectedFilter

    val jobs: StateFlow<List<JobEntity>> = combine(_allJobs, _selectedFilter) { allJobs, filter ->
        if (filter == null) allJobs
        else allJobs.filter { it.status == filter.value }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableStateFlow<ClientDetailEvent>(ClientDetailEvent.None)
    val event: StateFlow<ClientDetailEvent> = _event

    init {
        loadClientData()
    }

    private fun loadClientData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Cargar datos del cliente
            when (val result = clientRepository.getClientById(clientId)) {
                is Result.Success -> _client.value = result.data
                is Result.Error -> _event.value = ClientDetailEvent.Error("Error al cargar cliente")
                else -> {}
            }

            // Cargar trabajos del cliente
            getJobsByClientUseCase(clientId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _allJobs.value = result.data
                        _isLoading.value = false
                    }
                    is Result.Error -> {
                        _event.value = ClientDetailEvent.Error("Error al cargar trabajos")
                        _isLoading.value = false
                    }
                    is Result.Loading -> _isLoading.value = true
                }
            }
        }
    }

    fun onFilterSelected(status: JobStatus?) {
        _selectedFilter.value = status
    }

    fun deleteJob(job: JobEntity) {
        viewModelScope.launch {
            when (val result = deleteJobUseCase(job)) {
                is Result.Success -> {
                    _event.value = ClientDetailEvent.Success("Trabajo eliminado")
                }
                is Result.Error -> {
                    _event.value = ClientDetailEvent.Error("Error al eliminar trabajo")
                }
                else -> {}
            }
        }
    }

    fun exportJob(job: JobEntity) {
        val text = exportJobUseCase(job)
        _event.value = ClientDetailEvent.Export(text)
    }

    fun clearEvent() {
        _event.value = ClientDetailEvent.None
    }
}

sealed class ClientDetailEvent {
    data object None : ClientDetailEvent()
    data class Success(val message: String) : ClientDetailEvent()
    data class Error(val message: String) : ClientDetailEvent()
    data class Export(val text: String) : ClientDetailEvent()
}
