package com.tradejob.pro.home.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.UserEntity
import com.tradejob.pro.home.domain.UpdateUserProfileUseCase
import com.tradejob.pro.home.domain.UserRepository
import com.tradejob.pro.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _specialty = MutableStateFlow("")
    val specialty: StateFlow<String> = _specialty

    private val _nif = MutableStateFlow("")
    val nif: StateFlow<String> = _nif

    private val _businessName = MutableStateFlow("")
    val businessName: StateFlow<String> = _businessName

    private val _businessAddress = MutableStateFlow("")
    val businessAddress: StateFlow<String> = _businessAddress

    private val _businessCity = MutableStateFlow("")
    val businessCity: StateFlow<String> = _businessCity

    private val _irpfRate = MutableStateFlow("")
    val irpfRate: StateFlow<String> = _irpfRate

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableStateFlow<ProfileEvent>(ProfileEvent.None)
    val event: StateFlow<ProfileEvent> = _event

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            val profileFlow = if (userId != -1L) {
                userRepository.getUserProfileById(userId)
            } else {
                userRepository.getUserProfile()
            }
            
            profileFlow.collectLatest { userEntity ->
                userEntity?.let {
                    _user.value = it
                    _name.value = it.name
                    _phone.value = it.phone.orEmpty()
                    _specialty.value = it.specialty.orEmpty()
                    _nif.value = it.nif.orEmpty()
                    _businessName.value = it.businessName.orEmpty()
                    _businessAddress.value = it.businessAddress.orEmpty()
                    _businessCity.value = it.businessCity.orEmpty()
                    _irpfRate.value = it.irpfRate?.toString().orEmpty()
                }
            }
        }
    }

    fun onNameChanged(value: String) {
        _name.value = value
    }

    fun onPhoneChanged(value: String) {
        _phone.value = value
    }

    fun onSpecialtyChanged(value: String) {
        _specialty.value = value
    }

    fun onNifChanged(value: String) { _nif.value = value }
    fun onBusinessNameChanged(value: String) { _businessName.value = value }
    fun onBusinessAddressChanged(value: String) { _businessAddress.value = value }
    fun onBusinessCityChanged(value: String) { _businessCity.value = value }
    fun onIrpfRateChanged(value: String) { _irpfRate.value = value }

    fun saveProfile() {
        val currentUser = _user.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updatedUser = currentUser.copy(
                name = _name.value,
                phone = _phone.value,
                specialty = _specialty.value,
                nif = _nif.value.ifBlank { null },
                businessName = _businessName.value.ifBlank { null },
                businessAddress = _businessAddress.value.ifBlank { null },
                businessCity = _businessCity.value.ifBlank { null },
                irpfRate = _irpfRate.value.toDoubleOrNull()
            )
            when (val result = updateUserProfileUseCase(updatedUser)) {
                is Result.Success -> {
                    _event.value = ProfileEvent.Success("Perfil actualizado con éxito")
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _event.value = ProfileEvent.Error("Error al actualizar perfil")
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }

    fun clearEvent() {
        _event.value = ProfileEvent.None
    }
}

sealed class ProfileEvent {
    data object None : ProfileEvent()
    data class Success(val message: String) : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
}
