package com.tradejob.pro.login.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.login.ui.LoginUIModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: LoginRepository) {

    suspend operator fun invoke(input: Input): Flow<Result<LoginUIModel>> = flow {
        emit(Result.Loading)
        val loginResult = repository.login(input.email, input.password)
        when (val response = loginResult.response) {
            is Result.Success -> {
                emit(Result.Success(LoginUIModel(response.data.message, loginResult.userId)))
            }
            is Result.Error -> {
                emit(Result.Error(response.exception))
            }
            is Result.Loading -> {
                emit(Result.Loading)
            }
        }
    }
}
