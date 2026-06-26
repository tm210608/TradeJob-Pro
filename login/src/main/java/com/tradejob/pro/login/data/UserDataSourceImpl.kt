package com.tradejob.pro.login.data

import com.tradejob.pro.database.data.dao.UserDao
import com.tradejob.pro.database.data.entity.UserEntity
import com.tradejob.pro.login.domain.UserDataSource
import com.tradejob.pro.network.dummy_login.domain.User
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

class UserDataSourceImpl @Inject constructor(
    private val userDao: UserDao
) : UserDataSource {

    override suspend fun registerUser(user: User): Long {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        val userWithHashedPassword = user.copy(password = hashedPassword)
        return userDao.insert(userWithHashedPassword.toEntity())
    }

    override suspend fun checkCredentialsDataBase(email: String, password: String): Long? {
        val userEntity = userDao.getUserByEmail(email) ?: return null
        return if (userEntity.firebaseUid != null) {
            // Si ya tiene Firebase UID, la contraseña local no es lo que importa para el login legacy
            // Pero para mantener compatibilidad si no usa Firebase:
            if (BCrypt.checkpw(password, userEntity.password)) userEntity.id else null
        } else {
            if (BCrypt.checkpw(password, userEntity.password)) {
                userEntity.id
            } else {
                null
            }
        }
    }

    override suspend fun getUserByFirebaseUid(uid: String): Long? {
        return userDao.getUserIdByFirebaseUid(uid)
    }

    override suspend fun updateFirebaseUid(userId: Long, uid: String) {
        userDao.updateFirebaseUid(userId, uid)
    }
}

private fun User.toEntity(): UserEntity {
    return UserEntity(
        name = name,
        email = email,
        password = password,
        phone = phone,
        firebaseUid = null // Se actualizará tras el registro en Firebase
    )
}
