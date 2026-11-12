package com.tradejob.pro.login.domain

import com.tradejob.pro.common.usecase.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {

    private val repository: LoginRepository = mockk()
    private val useCase = LoginUseCase(repository)

    @Test
    fun `emite Loading y Success con mensaje y userId del repositorio`() = runTest {
        coEvery { repository.login("tecnico@tradejob.es", "1234567") } returns LoginResult(
            response = Result.Success(LoginResponse(status = "ok", message = "Bienvenido")),
            userId = 7L
        )

        val emissions = useCase(Input("tecnico@tradejob.es", "1234567")).toList()

        assertTrue(emissions.first() is Result.Loading)
        val success = emissions.last()
        assertTrue(success is Result.Success)
        assertEquals("Bienvenido", (success as Result.Success).data.message)
        assertEquals(7L, success.data.userId)
    }

    @Test
    fun `emite Loading y Error cuando el repositorio falla`() = runTest {
        val error = Exception("Sin conexión")
        coEvery { repository.login("tecnico@tradejob.es", "1234567") } returns LoginResult(
            response = Result.Error(error),
            userId = null
        )

        val emissions = useCase(Input("tecnico@tradejob.es", "1234567")).toList()

        assertTrue(emissions.first() is Result.Loading)
        val result = emissions.last()
        assertTrue(result is Result.Error)
        assertEquals(error, (result as Result.Error).exception)
    }
}
