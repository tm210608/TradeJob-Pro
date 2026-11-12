package com.tradejob.pro.login.domain

import com.google.firebase.auth.FirebaseUser
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.login.data.FirebaseAuthDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUserUseCaseTest {

    private val firebaseAuthDataSource: FirebaseAuthDataSource = mockk()
    private val userDataSource: UserDataSource = mockk()
    private val useCase = RegisterUserUseCase(firebaseAuthDataSource, userDataSource)

    private val user = User(name = "Técnico", email = "tecnico@tradejob.es", password = "123456")

    @Test
    fun `exito firebase registra local vincula uid y emite Success`() = runTest {
        val firebaseUser = mockk<FirebaseUser> { every { uid } returns "uid-firebase-123" }
        coEvery { firebaseAuthDataSource.register(user.email, user.password) } returns
            Result.Success(firebaseUser)
        coEvery { userDataSource.registerUser(user) } returns 9L
        coEvery { userDataSource.updateFirebaseUid(9L, "uid-firebase-123") } returns Unit

        val emissions = useCase(user).toList()

        assertTrue(emissions.first() is Result.Loading)
        val result = emissions.last()
        assertTrue(result is Result.Success)
        assertEquals(9L, (result as Result.Success).data)
        coVerify { userDataSource.updateFirebaseUid(9L, "uid-firebase-123") }
    }

    @Test
    fun `error firebase emite Error sin tocar el almacen local`() = runTest {
        val error = Exception("El email ya está registrado")
        coEvery { firebaseAuthDataSource.register(user.email, user.password) } returns
            Result.Error(error)

        val emissions = useCase(user).toList()

        assertTrue(emissions.first() is Result.Loading)
        val result = emissions.last()
        assertTrue(result is Result.Error)
        assertEquals(error, (result as Result.Error).exception)
        coVerify(exactly = 0) { userDataSource.registerUser(any()) }
        coVerify(exactly = 0) { userDataSource.updateFirebaseUid(any(), any()) }
    }

    @Test
    fun `fallo en registro local emite Error tras exito firebase`() = runTest {
        coEvery { firebaseAuthDataSource.register(user.email, user.password) } returns
            Result.Success(null)
        val dbError = Exception("Base de datos llena")
        coEvery { userDataSource.registerUser(user) } throws dbError

        val emissions = useCase(user).toList()

        val result = emissions.last()
        assertTrue(result is Result.Error)
        assertEquals(dbError, (result as Result.Error).exception)
    }
}
