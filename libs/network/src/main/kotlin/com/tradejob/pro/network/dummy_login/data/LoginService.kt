package com.tradejob.pro.network.dummy_login.data

import com.tradejob.pro.network.dummy_login.data.request.LoginRequest
import com.tradejob.pro.network.dummy_login.data.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
