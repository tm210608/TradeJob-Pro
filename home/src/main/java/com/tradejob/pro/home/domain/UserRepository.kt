package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<UserEntity?>
    fun getUserProfileById(id: Long): Flow<UserEntity?>
    suspend fun updateUserProfile(user: UserEntity): Result<Unit>
}
