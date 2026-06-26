package com.tradejob.pro.login.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tradejob.pro.common.usecase.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor() {
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private fun isDummyConfig(): Boolean {
        val app = auth?.app ?: return true
        val projectId = app.options.projectId ?: ""
        val apiKey = app.options.apiKey ?: ""
        return projectId.contains("dummy") || apiKey.contains("DummyKey")
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser?> {
        if (isDummyConfig()) {
            return Result.Success(null)
        }
        val authInstance = auth ?: return Result.Error(Exception("Firebase Auth not available"))
        return try {
            val result = authInstance.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun register(email: String, password: String): Result<FirebaseUser?> {
        if (isDummyConfig()) {
            return Result.Success(null)
        }
        val authInstance = auth ?: return Result.Error(Exception("Firebase Auth not available"))
        return try {
            val result = authInstance.createUserWithEmailAndPassword(email, password).await()
            Result.Success(result.user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth?.currentUser

    fun logout() {
        auth?.signOut()
    }
}
