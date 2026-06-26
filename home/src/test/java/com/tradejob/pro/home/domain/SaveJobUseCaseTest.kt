package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveJobUseCaseTest {

    private val repository: JobRepository = mockk()
    private val saveJobUseCase = SaveJobUseCase(repository)

    @Test
    fun `when inserting new job then return success result with id`() = runTest {
        // Given
        val job = JobEntity(clientId = 1L, title = "Test Job")
        coEvery { repository.insertJob(job) } returns Result.Success(10L)

        // When
        val result = saveJobUseCase(job, isNew = true)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(10L, (result as Result.Success).data)
    }

    @Test
    fun `when updating existing job then return success result with id`() = runTest {
        // Given
        val job = JobEntity(id = 5L, clientId = 1L, title = "Updated Job")
        coEvery { repository.updateJob(job) } returns Result.Success(Unit)

        // When
        val result = saveJobUseCase(job, isNew = false)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(5L, (result as Result.Success).data)
    }

    @Test
    fun `when repository returns error then return error result`() = runTest {
        // Given
        val job = JobEntity(clientId = 1L, title = "Test Job")
        val exception = Exception("DB Error")
        coEvery { repository.insertJob(job) } returns Result.Error(exception)

        // When
        val result = saveJobUseCase(job, isNew = true)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }
}
