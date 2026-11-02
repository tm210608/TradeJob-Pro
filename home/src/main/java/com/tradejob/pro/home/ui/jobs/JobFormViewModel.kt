package com.tradejob.pro.home.ui.jobs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.common.validation.NumberValidator
import com.tradejob.pro.common.validation.RequiredValidator
import com.tradejob.pro.common.validation.ValidationResult
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import com.tradejob.pro.home.R
import com.tradejob.pro.home.domain.AddJobPhotoUseCase
import com.tradejob.pro.home.domain.DeleteJobPhotoUseCase
import com.tradejob.pro.home.domain.GetJobByIdUseCase
import com.tradejob.pro.home.domain.GetPhotosByJobUseCase
import com.tradejob.pro.home.domain.SaveJobUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobFormViewModel @Inject constructor(
    private val saveJobUseCase: SaveJobUseCase,
    private val getJobByIdUseCase: GetJobByIdUseCase,
    private val getPhotosByJobUseCase: GetPhotosByJobUseCase,
    private val addJobPhotoUseCase: AddJobPhotoUseCase,
    private val deleteJobPhotoUseCase: DeleteJobPhotoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clientId: Long = savedStateHandle.get<String>("clientId")?.toLongOrNull() ?: 0L
    val jobId: Long? = savedStateHandle.get<String>("jobId")?.toLongOrNull()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _status = MutableStateFlow(JobStatus.PENDING)
    val status: StateFlow<JobStatus> = _status

    private val _priority = MutableStateFlow(JobPriority.MEDIUM)
    val priority: StateFlow<JobPriority> = _priority

    private val _titleError = MutableStateFlow<ValidationResult>(ValidationResult.Success)
    val titleError: StateFlow<ValidationResult> = _titleError

    private val _budgetAmount = MutableStateFlow("")
    val budgetAmount: StateFlow<String> = _budgetAmount

    private val _budgetError = MutableStateFlow<ValidationResult>(ValidationResult.Success)
    val budgetError: StateFlow<ValidationResult> = _budgetError

    private val _finalAmount = MutableStateFlow("")
    val finalAmount: StateFlow<String> = _finalAmount

    private val _finalAmountError = MutableStateFlow<ValidationResult>(ValidationResult.Success)
    val finalAmountError: StateFlow<ValidationResult> = _finalAmountError

    private val _scheduledAt = MutableStateFlow<Long?>(null)
    val scheduledAt: StateFlow<Long?> = _scheduledAt

    private val _photos = MutableStateFlow<List<JobPhotoEntity>>(emptyList())
    val photos: StateFlow<List<JobPhotoEntity>> = _photos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableStateFlow<JobFormEvent>(JobFormEvent.None)
    val event: StateFlow<JobFormEvent> = _event

    private var loadedJob: JobEntity? = null
    private val _tempPhotos = MutableStateFlow<List<String>>(emptyList())

    val isEditMode: Boolean get() = jobId != null && jobId != 0L

    private val titleValidator = RequiredValidator(com.tradejob.pro.common.R.string.validation_required)
    private val amountValidator = NumberValidator(com.tradejob.pro.common.R.string.validation_invalid_number)

    init {
        if (isEditMode) {
            jobId?.let { 
                loadJob(it)
                loadPhotos(it)
            }
        }
    }

    private fun loadJob(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getJobByIdUseCase(id)) {
                is Result.Success -> {
                    result.data?.let { job ->
                        loadedJob = job
                        _title.value = job.title
                        _description.value = job.description.orEmpty()
                        _status.value = JobStatus.fromValue(job.status)
                        _priority.value = JobPriority.fromValue(job.priority)
                        _budgetAmount.value = job.budgetAmount?.toString() ?: ""
                        _finalAmount.value = job.finalAmount?.toString() ?: ""
                        _scheduledAt.value = job.scheduledAt
                    }
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _event.value = JobFormEvent.Error("Error al cargar trabajo")
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }

    private fun loadPhotos(id: Long) {
        viewModelScope.launch {
            getPhotosByJobUseCase(id).collect { result ->
                if (result is Result.Success) {
                    _photos.value = result.data
                }
            }
        }
    }

    fun onTitleChanged(value: String) {
        _title.value = value
        _titleError.value = titleValidator.validate(value)
    }

    fun onDescriptionChanged(value: String) {
        _description.value = value
    }

    fun onStatusChanged(value: JobStatus) {
        _status.value = value
    }

    fun onPriorityChanged(value: JobPriority) {
        _priority.value = value
    }

    fun onBudgetAmountChanged(value: String) {
        _budgetAmount.value = value
        _budgetError.value = amountValidator.validate(value)
    }

    fun onFinalAmountChanged(value: String) {
        _finalAmount.value = value
        _finalAmountError.value = amountValidator.validate(value)
    }

    fun onScheduledAtChanged(value: Long?) {
        _scheduledAt.value = value
    }

    fun addPhoto(path: String, type: String = "BEFORE") {
        if (isEditMode) {
            val currentJobId = jobId ?: return
            viewModelScope.launch {
                val photo = JobPhotoEntity(
                    jobId = currentJobId,
                    photoPath = path,
                    type = type
                )
                when (addJobPhotoUseCase(photo)) {
                    is Result.Success -> loadPhotos(currentJobId)
                    is Result.Error -> _event.value = JobFormEvent.Error("Error al añadir foto")
                    else -> {}
                }
            }
        } else {
            // Para trabajos nuevos, guardamos temporalmente la ruta
            _tempPhotos.value = _tempPhotos.value + path
            updatePhotosListFromTemp()
        }
    }

    fun deletePhoto(photo: JobPhotoEntity) {
        if (isEditMode) {
            viewModelScope.launch {
                when (deleteJobPhotoUseCase(photo)) {
                    is Result.Success -> loadPhotos(jobId!!)
                    is Result.Error -> _event.value = JobFormEvent.Error("Error al eliminar foto")
                    else -> {}
                }
            }
        } else {
            // Para trabajos nuevos, eliminamos de la lista temporal
            _tempPhotos.value = _tempPhotos.value.filter { it != photo.photoPath }
            updatePhotosListFromTemp()
        }
    }

    private fun updatePhotosListFromTemp() {
        val mockPhotos = _tempPhotos.value.map {
            JobPhotoEntity(jobId = 0, photoPath = it, type = "BEFORE")
        }
        _photos.value = mockPhotos
    }

    fun save() {
        _titleError.value = titleValidator.validate(_title.value)
        _budgetError.value = amountValidator.validate(_budgetAmount.value)
        _finalAmountError.value = amountValidator.validate(_finalAmount.value)

        if (_titleError.value is ValidationResult.Error ||
            _budgetError.value is ValidationResult.Error ||
            _finalAmountError.value is ValidationResult.Error
        ) {
            _event.value = JobFormEvent.Error("Por favor, corrija los errores en el formulario")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val job = JobEntity(
                id = if (isEditMode) jobId ?: 0L else 0L,
                clientId = clientId,
                title = _title.value.trim(),
                description = _description.value.trim(),
                status = _status.value.value,
                priority = _priority.value.value,
                budgetAmount = _budgetAmount.value.toDoubleOrNull(),
                finalAmount = _finalAmount.value.toDoubleOrNull(),
                scheduledAt = _scheduledAt.value,
                createdAt = loadedJob?.createdAt ?: System.currentTimeMillis()
            )

            when (val result = saveJobUseCase(job, !isEditMode)) {
                is Result.Success -> {
                    // Si era un trabajo nuevo, guardar las fotos ahora que tenemos el ID
                    if (!isEditMode) {
                        val newJobId = result.data
                        _tempPhotos.value.forEach { path ->
                            addJobPhotoUseCase(JobPhotoEntity(jobId = newJobId, photoPath = path))
                        }
                    }
                    _event.value = JobFormEvent.Success(
                        if (isEditMode) "Trabajo actualizado" else "Trabajo creado"
                    )
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _event.value = JobFormEvent.Error("Error al guardar trabajo")
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }

    fun clearEvent() {
        _event.value = JobFormEvent.None
    }
}

sealed class JobFormEvent {
    data object None : JobFormEvent()
    data class Success(val message: String) : JobFormEvent()
    data class Error(val message: String) : JobFormEvent()
}
