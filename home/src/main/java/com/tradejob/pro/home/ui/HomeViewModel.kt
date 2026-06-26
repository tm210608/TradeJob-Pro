package com.tradejob.pro.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradejob.pro.home.domain.GetHomeStatsUseCase
import com.tradejob.pro.home.domain.HomeStats
import com.tradejob.pro.home.domain.UserRepository
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getHomeStatsUseCase: GetHomeStatsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    val userName: StateFlow<String> = flow {
        val userId = sessionManager.getUserId()
        if (userId != -1L) {
            emitAll(userRepository.getUserProfileById(userId))
        } else {
            emitAll(userRepository.getUserProfile())
        }
    }.map { it?.name ?: "Usuario" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Usuario"
        )

    val stats: StateFlow<Result<HomeStats>> = getHomeStatsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Result.Loading
        )

    private val _menuExpanded = MutableStateFlow(false)
    val menuExpanded: StateFlow<Boolean> = _menuExpanded

    fun onMenuClick() {
        _menuExpanded.value = !_menuExpanded.value
    }

    fun dismissMenu() {
        _menuExpanded.value = false
    }

    fun logout() {
        sessionManager.logout()
    }
}
