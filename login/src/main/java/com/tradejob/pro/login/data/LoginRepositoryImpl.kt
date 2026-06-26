package com.tradejob.pro.login.data

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.login.domain.LoginRepository
import com.tradejob.pro.login.domain.LoginResult
import com.tradejob.pro.login.domain.UserDataSource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.tradejob.pro.network.dummy_login.data.response.LoginResponse

class LoginRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val userDataSource: UserDataSource
) : LoginRepository {

    override suspend fun login(
        email: String,
        password: String
    ): LoginResult {
        return withContext(Dispatchers.IO) {
            // 1. Intentar login con Firebase
            val firebaseResult = firebaseAuthDataSource.login(email, password)
            
            when (firebaseResult) {
                is Result.Success -> {
                    val user = firebaseResult.data
                    val uid = user?.uid ?: ""
                    
                    // 2. Buscar o vincular usuario localmente
                    var userId = userDataSource.getUserByFirebaseUid(uid)
                    
                    if (userId == null) {
                        // Intentar buscar por email (migración de usuarios locales a Firebase)
                        userId = userDataSource.checkCredentialsDataBase(email, password)
                        if (userId != null) {
                            userDataSource.updateFirebaseUid(userId, uid)
                        }
                    }
                    
                    LoginResult(
                        response = Result.Success(LoginResponse("ok", "Sesión iniciada con Firebase")),
                        userId = userId
                    )
                }
                is Result.Error -> {
                    // Fallback a login local si Firebase falla (opcional, dependiendo de la política)
                    val localUserId = userDataSource.checkCredentialsDataBase(email, password)
                    if (localUserId != null) {
                         LoginResult(
                            response = Result.Success(LoginResponse("ok", "Sesión local iniciada (Offline)")),
                            userId = localUserId
                        )
                    } else {
                        LoginResult(Result.Error(firebaseResult.exception), null)
                    }
                }
                else -> LoginResult(Result.Loading, null)
            }
        }
    }
}
