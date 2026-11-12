@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.login.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.common.analytics.AnalyticsHelper
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.common.validation.EmailValidator
import com.tradejob.pro.login.domain.LoginUseCase
import com.tradejob.pro.login.domain.Input
import com.tradejob.pro.core.session.SessionManager
import com.tradejob.pro.database.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalMaterial3Api
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val sessionManager: SessionManager,
    private val syncManager: SyncManager,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _status: MutableStateFlow<Status> = MutableStateFlow(Status())
    val status: StateFlow<Status> = _status

    private val _event: MutableStateFlow<Event> = MutableStateFlow(Event.None)
    val event: StateFlow<Event> = _event

    fun login() {
        viewModelScope.launch {
            loginUseCase(Input(status.value.email, status.value.password))
                .onStart {
                    _status.value = _status.value.copy(isLoading = true)
                    _event.emit(Event.Loading)
                }
                .collect { result ->
                    _status.value = _status.value.copy(isLoading = false)
                    when (result) {
                        is Result.Error -> {
                            analyticsHelper.logError(result.exception, "Login failed for ${status.value.email}")
                            _event.emit(Event.Error(result.exception.message ?: "Unknown error"))
                        }
                        is Result.Success<LoginUIModel> -> {
                            analyticsHelper.logEvent("login_success", mapOf("email" to status.value.email))
                            result.data.userId?.let { 
                                sessionManager.saveUserId(it)
                                analyticsHelper.setUserId(it.toString())
                            }
                            _status.value = status.value.copy(userId = result.data.userId)
                            syncManager.scheduleSync()
                            _event.emit(Event.Success(result.data.message))
                        }
                        else -> {}
                    }
                }
        }
    }

    fun onLoginChanged(email: String, password: String) {
        _status.value = _status.value.copy(
            email = email,
            password = password,
            loginEnable = isValidEmail(email) && isValidPassword(password)
        )
    }

    private fun isValidPassword(password: String): Boolean = password.length >= 6

    private fun isValidEmail(email: String): Boolean = EmailValidator.isValid(email)

    fun clearEvent() {
        viewModelScope.launch { _event.emit(Event.None) }
    }

    fun showCloseDialog() {
        _status.value = _status.value.copy(sheetValue = SheetValue.Expanded)
    }

    fun hideCloseDialog() {
        _status.value = _status.value.copy(sheetValue = SheetValue.Hidden)
    }
}

sealed class Event {
    data object Loading : Event()
    data class Success(val message: String) : Event()
    data class Error(val message: String) : Event()
    data object None : Event()
}

data class Status(
    val email: String = "",
    val password: String = "",
    val loginEnable: Boolean = false,
    val isLoading: Boolean = false,
    val userId: Long? = null,
    val sheetValue: SheetValue = SheetValue.Hidden
)
