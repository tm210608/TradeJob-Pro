package com.tradejob.pro.home.ui.profile

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.core.session.SessionManager
import com.tradejob.pro.database.data.entity.UserEntity
import com.tradejob.pro.home.domain.UpdateUserProfileUseCase
import com.tradejob.pro.home.domain.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val userRepository: UserRepository = mockk()
    private val updateUserProfileUseCase: UpdateUserProfileUseCase = mockk()
    private val sessionManager: SessionManager = mockk()

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when viewmodel starts then loads user profile`() = runTest {
        // Given
        val user = UserEntity(
            id = 1L,
            name = "John Doe",
            email = "john@example.com",
            password = "pass",
            phone = "123456",
            specialty = "Electrician"
        )
        coEvery { sessionManager.getUserId() } returns 1L
        every { userRepository.getUserProfileById(1L) } returns flowOf(user)

        // When
        viewModel = ProfileViewModel(userRepository, updateUserProfileUseCase, sessionManager)

        // Then
        assertEquals("John Doe", viewModel.name.value)
        assertEquals("123456", viewModel.phone.value)
        assertEquals("Electrician", viewModel.specialty.value)
        assertEquals(user, viewModel.user.value)
    }

    @Test
    fun `when fields change then state is updated`() = runTest {
        // Given
        val user = UserEntity(id = 1L, name = "John Doe", email = "john@example.com", password = "pass")
        coEvery { sessionManager.getUserId() } returns 1L
        every { userRepository.getUserProfileById(1L) } returns flowOf(user)
        viewModel = ProfileViewModel(userRepository, updateUserProfileUseCase, sessionManager)

        // When
        viewModel.onNameChanged("Jane Doe")
        viewModel.onPhoneChanged("654321")
        viewModel.onSpecialtyChanged("Plumber")

        // Then
        assertEquals("Jane Doe", viewModel.name.value)
        assertEquals("654321", viewModel.phone.value)
        assertEquals("Plumber", viewModel.specialty.value)
    }

    @Test
    fun `when save profile successful then success event is emitted`() = runTest {
        // Given
        val user = UserEntity(id = 1L, name = "John Doe", email = "john@example.com", password = "pass")
        coEvery { sessionManager.getUserId() } returns 1L
        every { userRepository.getUserProfileById(1L) } returns flowOf(user)
        coEvery { updateUserProfileUseCase(any()) } returns Result.Success(Unit)
        
        viewModel = ProfileViewModel(userRepository, updateUserProfileUseCase, sessionManager)
        viewModel.onNameChanged("John Updated")

        // When
        viewModel.saveProfile()

        // Then
        assertTrue(viewModel.event.value is ProfileEvent.Success)
        assertEquals("Perfil actualizado con éxito", (viewModel.event.value as ProfileEvent.Success).message)
    }

    @Test
    fun `when save profile fails then error event is emitted`() = runTest {
        // Given
        val user = UserEntity(id = 1L, name = "John Doe", email = "john@example.com", password = "pass")
        coEvery { sessionManager.getUserId() } returns 1L
        every { userRepository.getUserProfileById(1L) } returns flowOf(user)
        coEvery { updateUserProfileUseCase(any()) } returns Result.Error(Exception("Network error"))
        
        viewModel = ProfileViewModel(userRepository, updateUserProfileUseCase, sessionManager)

        // When
        viewModel.saveProfile()

        // Then
        assertTrue(viewModel.event.value is ProfileEvent.Error)
        assertEquals("Error al actualizar perfil", (viewModel.event.value as ProfileEvent.Error).message)
    }
}
