package com.tradejob.pro.login.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.network.dummy_login.domain.User
import com.tradejob.pro.login.data.FirebaseAuthDataSource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val userDataSource: UserDataSource
) {
    suspend operator fun invoke(user: User): Flow<Result<Long>> = flow {
        emit(Result.Loading)
        
        // 1. Registro en Firebase
        val firebaseResult = firebaseAuthDataSource.register(user.email, user.password)
        
        when (firebaseResult) {
            is Result.Success -> {
                val firebaseUser = firebaseResult.data
                val uid = firebaseUser?.uid ?: "dummy_uid_${System.currentTimeMillis()}"
                
                try {
                    // 2. Registro local vinculado
                    val localId = userDataSource.registerUser(user)
                    userDataSource.updateFirebaseUid(localId, uid)
                    emit(Result.Success(localId))
                } catch (e: Exception) {
                    emit(Result.Error(e))
                }
            }
            is Result.Error -> {
                emit(Result.Error(firebaseResult.exception))
            }
            else -> {}
        }
    }
}
