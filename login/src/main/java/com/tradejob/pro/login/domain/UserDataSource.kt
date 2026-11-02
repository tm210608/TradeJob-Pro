package com.tradejob.pro.login.domain


interface UserDataSource {
    suspend fun registerUser(user: User): Long
    suspend fun checkCredentialsDataBase(email: String, password: String): Long?
    suspend fun getUserByFirebaseUid(uid: String): Long?
    suspend fun updateFirebaseUid(userId: Long, uid: String)
}
