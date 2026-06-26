package com.tradejob.pro.home.data

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.dao.UserDao
import com.tradejob.pro.database.data.entity.UserEntity
import com.tradejob.pro.home.domain.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {
    override fun getUserProfile(): Flow<UserEntity?> = userDao.getUserProfile()

    override fun getUserProfileById(id: Long): Flow<UserEntity?> = userDao.getUserProfileById(id)

    override suspend fun updateUserProfile(user: UserEntity): Result<Unit> {
        return try {
            userDao.update(user)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
