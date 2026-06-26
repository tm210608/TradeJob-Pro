package com.tradejob.pro.login.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.network.dummy_login.data.response.LoginResponse

interface LoginRemoteDataSource {
    suspend fun login(email: String, password: String): Result<LoginResponse>
}
