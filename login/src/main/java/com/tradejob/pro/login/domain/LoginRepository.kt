package com.tradejob.pro.login.domain

import com.tradejob.pro.common.usecase.Result

data class LoginResult(val response: Result<LoginResponse>, val userId: Long?)

interface LoginRepository {
    suspend fun login(email: String, password: String): LoginResult
}
