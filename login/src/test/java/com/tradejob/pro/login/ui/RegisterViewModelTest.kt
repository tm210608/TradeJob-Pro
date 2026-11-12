@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.login.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.login.domain.RegisterUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val registerUserUseCase: RegisterUserUseCase = mockk()
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = RegisterViewModel(registerUserUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillValidForm(password: String = "123456") {
        viewModel.onNameChanged("Técnico Prueba")
        viewModel.onEmailChanged("tecnico@tradejob.es")
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)
    }

    @Test
    fun `isValid solo con todos los campos correctos`() {
        fillValidForm()
        assertTrue(viewModel.status.value.isValid)
    }

    @Test
    fun `isValid falso si las contrasenas no coinciden`() {
        fillValidForm()
        viewModel.onConfirmPasswordChanged("otra-cosa")
        assertFalse(viewModel.status.value.isValid)
    }

    @Test
    fun `isValid falso con nombre vacio`() {
        fillValidForm()
        viewModel.onNameChanged("   ")
        assertFalse(viewModel.status.value.isValid)
    }

    @Test
    fun `isValid falso con email invalido`() {
        viewModel.onNameChanged("Técnico Prueba")
        viewModel.onEmailChanged("correo-malo")
        viewModel.onPasswordChanged("123456")
        viewModel.onConfirmPasswordChanged("123456")
        assertFalse(viewModel.status.value.isValid)
    }

    @Test
    fun `isValid acepta password de 6 caracteres exactos`() {
        // El registro exige >= 6 (comportamiento actual; ver hallazgo en SPEC-008:
        // el login pide > 6, asi que esta contrasena fallaria despues al entrar)
        fillValidForm(password = "123456")
        assertTrue(viewModel.status.value.isValid)
    }

    @Test
    fun `register invalido emite Error sin llamar al use case`() = runTest {
        viewModel.register()

        assertTrue(viewModel.event.value is RegisterEvent.Error)
        coVerify(exactly = 0) { registerUserUseCase(any()) }
    }

    @Test
    fun `register con exito emite Success`() = runTest {
        coEvery { registerUserUseCase(any()) } returns flowOf(Result.Success(9L))
        fillValidForm()

        viewModel.register()

        assertEquals(RegisterEvent.Success("Usuario registrado correctamente"), viewModel.event.value)
        assertFalse(viewModel.status.value.isLoading)
    }

    @Test
    fun `register con error del use case emite Error con el mensaje`() = runTest {
        coEvery { registerUserUseCase(any()) } returns flowOf(
            Result.Error(Exception("El email ya está registrado"))
        )
        fillValidForm()

        viewModel.register()

        assertEquals(RegisterEvent.Error("El email ya está registrado"), viewModel.event.value)
        assertFalse(viewModel.status.value.isLoading)
    }

    @Test
    fun `clearEvent vuelve el evento a None`() = runTest {
        viewModel.register()
        assertTrue(viewModel.event.value is RegisterEvent.Error)

        viewModel.clearEvent()

        assertEquals(RegisterEvent.None, viewModel.event.value)
    }
}
