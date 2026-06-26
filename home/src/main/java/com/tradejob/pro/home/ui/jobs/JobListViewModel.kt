package com.tradejob.pro.home.ui.jobs

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.GeneratePdfReportUseCase
import com.tradejob.pro.home.domain.GetJobsByClientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class JobListViewModel @Inject constructor(
    private val getJobsByClientUseCase: GetJobsByClientUseCase,
    private val deleteJobUseCase: com.tradejob.pro.home.domain.DeleteJobUseCase,
    private val exportJobUseCase: com.tradejob.pro.home.domain.ExportJobUseCase,
    private val generatePdfReportUseCase: GeneratePdfReportUseCase,
    private val clientRepository: ClientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clientId: Long? = savedStateHandle.get<String>("clientId")?.toLongOrNull()

    private val _allJobs = MutableStateFlow<List<JobEntity>>(emptyList())
    
    private val _selectedFilter = MutableStateFlow<JobStatus?>(null) // null = All
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

    private val _event = MutableStateFlow<JobListEvent>(JobListEvent.None)
    val event: StateFlow<JobListEvent> = _event

    init {
        clientId?.let { loadJobs(it) }
    }

    private fun loadJobs(id: Long) {
        viewModelScope.launch {
            getJobsByClientUseCase(id).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _allJobs.value = result.data
                        _isLoading.value = false
                    }
                    is Result.Error -> {
                        _event.value = JobListEvent.Error("Error al cargar trabajos")
                        _isLoading.value = false
                    }
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
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
                    _event.value = JobListEvent.Success("Trabajo eliminado")
                }
                is Result.Error -> {
                    _event.value = JobListEvent.Error("Error al eliminar trabajo")
                }
                else -> {}
            }
        }
    }

    fun exportJob(job: JobEntity) {
        val text = exportJobUseCase(job)
        _event.value = JobListEvent.Export(text)
    }

    fun exportJobPdf(context: Context, job: JobEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            val clientResult = clientRepository.getClientById(job.clientId)
            if (clientResult is Result.Success) {
                val client = clientResult.data
                if (client != null) {
                    val file = generatePdfReportUseCase(context, client, job)
                    if (file != null) {
                        _event.value = JobListEvent.ExportPdf(file)
                    } else {
                        _event.value = JobListEvent.Error("Error al generar PDF")
                    }
                } else {
                    _event.value = JobListEvent.Error("Cliente no encontrado")
                }
            } else {
                _event.value = JobListEvent.Error("Error al obtener datos del cliente")
            }
            _isLoading.value = false
        }
    }

    fun clearEvent() {
        _event.value = JobListEvent.None
    }
}

sealed class JobListEvent {
    data object None : JobListEvent()
    data class Success(val message: String) : JobListEvent()
    data class Error(val message: String) : JobListEvent()
    data class Export(val text: String) : JobListEvent()
    data class ExportPdf(val file: File) : JobListEvent()
}
