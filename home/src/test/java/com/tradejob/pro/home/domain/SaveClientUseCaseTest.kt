package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveClientUseCaseTest {

    private val repository: ClientRepository = mockk()
    private val saveClientUseCase = SaveClientUseCase(repository)

    @Test
    fun `when inserting new client then return success result with id`() = runTest {
        // Given
        val client = ClientEntity(name = "Test", phone = "123")
        coEvery { repository.insertClient(client) } returns Result.Success(1L)

        // When
        val results = saveClientUseCase(client, isNew = true).toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals(1L, (results[1] as Result.Success).data)
    }

    @Test
    fun `when updating existing client then return success result with id`() = runTest {
        // Given
        val client = ClientEntity(id = 5L, name = "Update", phone = "456")
        coEvery { repository.updateClient(client) } returns Result.Success(Unit)

        // When
        val results = saveClientUseCase(client, isNew = false).toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals(5L, (results[1] as Result.Success).data)
    }
}
