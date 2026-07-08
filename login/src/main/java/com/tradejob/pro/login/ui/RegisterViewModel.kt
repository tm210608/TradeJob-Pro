@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.login.ui

import android.util.Patterns
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.login.domain.RegisterUserUseCase
import com.tradejob.pro.login.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalMaterial3Api
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _status = MutableStateFlow(RegisterStatus())
    val status: StateFlow<RegisterStatus> = _status

    private val _event = MutableStateFlow<RegisterEvent>(RegisterEvent.None)
    val event: StateFlow<RegisterEvent> = _event

    fun onNameChanged(name: String) {
        _status.value = _status.value.copy(name = name)
        updateValidation()
    }

    fun onEmailChanged(email: String) {
        _status.value = _status.value.copy(email = email)
        updateValidation()
    }

    fun onPasswordChanged(password: String) {
        _status.value = _status.value.copy(password = password)
        updateValidation()
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _status.value = _status.value.copy(confirmPassword = confirmPassword)
        updateValidation()
    }

    private fun updateValidation() {
        val currentStatus = _status.value
        val isValid = currentStatus.name.isNotBlank() &&
                isValidEmail(currentStatus.email) &&
                currentStatus.password.length >= 6 &&
                currentStatus.password == currentStatus.confirmPassword
        _status.value = currentStatus.copy(isValid = isValid)
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun register() {
        if (!_status.value.isValid) {
            _event.value = RegisterEvent.Error("Por favor completa todos los campos correctamente")
            return
        }

        viewModelScope.launch {
            _status.value = _status.value.copy(isLoading = true)
            _event.value = RegisterEvent.Loading
            
            val user = User(
                name = _status.value.name,
                email = _status.value.email,
                password = _status.value.password
            )

            registerUserUseCase(user).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _status.value = _status.value.copy(isLoading = false)
                        _event.value = RegisterEvent.Success("Usuario registrado correctamente")
                    }
                    is Result.Error -> {
                        _status.value = _status.value.copy(isLoading = false)
                        _event.value = RegisterEvent.Error(result.exception.message ?: "Error al registrar")
                    }
                    is Result.Loading -> {
                        _status.value = _status.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun clearEvent() {
        _event.value = RegisterEvent.None
    }
}

data class RegisterStatus(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isValid: Boolean = false,
    val isLoading: Boolean = false
)

sealed class RegisterEvent {
    data object None : RegisterEvent()
    data object Loading : RegisterEvent()
    data class Success(val message: String) : RegisterEvent()
    data class Error(val message: String) : RegisterEvent()
}
