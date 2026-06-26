package com.tradejob.pro.login.data

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.login.domain.LoginRemoteDataSource
import com.tradejob.pro.network.dummy_login.data.LoginService
import com.tradejob.pro.network.dummy_login.data.request.LoginRequest
import com.tradejob.pro.network.dummy_login.data.response.LoginResponse
import javax.inject.Inject

class LoginRemoteDataSourceImpl @Inject constructor(
    private val loginService: LoginService
): LoginRemoteDataSource {
    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        try {
            val response = loginService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return when {
                        body.status.equals(STATUS_SUCCESS, ignoreCase = true) -> Result.Success(body)
                        body.status.equals(STATUS_ERROR, ignoreCase = true) -> Result.Error(Exception(body.message))
                        else -> Result.Error(Exception("Unknown status"))
                    }
                } else {
                    return Result.Error(Exception("Response body is null"))
                }
            } else {
                return Result.Error(Exception("Response is not successful"))
            }
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }
    companion object {
        private const val STATUS_ERROR = "error"
        private const val STATUS_SUCCESS = "ok"
    }
}
