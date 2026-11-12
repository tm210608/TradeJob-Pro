@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.login.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import com.tradejob.pro.common.analytics.AnalyticsHelper
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.core.session.SessionManager
import com.tradejob.pro.database.sync.SyncManager
import com.tradejob.pro.login.domain.LoginUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class LoginViewModelTest {

    private val loginUseCase: LoginUseCase = mockk()
    private val sessionManager: SessionManager = mockk(relaxed = true)
    private val syncManager: SyncManager = mockk(relaxed = true)
    private val analyticsHelper: AnalyticsHelper = mockk(relaxed = true)
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LoginViewModel(loginUseCase, sessionManager, syncManager, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loginEnable solo con email valido y password mayor de 6`() {
        viewModel.onLoginChanged("tecnico@tradejob.es", "1234567")
        assertTrue(viewModel.status.value.loginEnable)
    }

    @Test
    fun `loginEnable desactivado con email invalido`() {
        viewModel.onLoginChanged("no-es-un-email", "1234567")
        assertFalse(viewModel.status.value.loginEnable)

        viewModel.onLoginChanged("falta@dominio", "1234567")
        assertFalse(viewModel.status.value.loginEnable)
    }

    @Test
    fun `loginEnable desactivado con password de 6 caracteres exactos`() {
        // El login exige > 6 caracteres (comportamiento actual; ver hallazgo en SPEC-008)
        viewModel.onLoginChanged("tecnico@tradejob.es", "123456")
        assertFalse(viewModel.status.value.loginEnable)
    }

    @Test
    fun `loginEnable desactivado con email vacio`() {
        viewModel.onLoginChanged("", "1234567")
        assertFalse(viewModel.status.value.loginEnable)
    }

    @Test
    fun `login con exito emite Success, guarda sesion y programa sync`() = runTest {
        coEvery { loginUseCase(any()) } returns flowOf(
            Result.Loading,
            Result.Success(LoginUIModel("Bienvenido", 42L))
        )
        viewModel.onLoginChanged("tecnico@tradejob.es", "1234567")

        viewModel.login()

        assertEquals(Event.Success("Bienvenido"), viewModel.event.value)
        assertEquals(42L, viewModel.status.value.userId)
        assertFalse(viewModel.status.value.isLoading)
        verify { sessionManager.saveUserId(42L) }
        verify { analyticsHelper.setUserId("42") }
        verify { syncManager.scheduleSync() }
    }

    @Test
    fun `login con exito pero userId nulo no guarda sesion`() = runTest {
        coEvery { loginUseCase(any()) } returns flowOf(
            Result.Success(LoginUIModel("Bienvenido", null))
        )
        viewModel.onLoginChanged("tecnico@tradejob.es", "1234567")

        viewModel.login()

        assertEquals(Event.Success("Bienvenido"), viewModel.event.value)
        verify(exactly = 0) { sessionManager.saveUserId(any()) }
        verify { syncManager.scheduleSync() }
    }

    @Test
    fun `login con error emite Event Error con el mensaje`() = runTest {
        coEvery { loginUseCase(any()) } returns flowOf(
            Result.Error(Exception("Credenciales incorrectas"))
        )
        viewModel.onLoginChanged("tecnico@tradejob.es", "1234567")

        viewModel.login()

        assertEquals(Event.Error("Credenciales incorrectas"), viewModel.event.value)
        assertFalse(viewModel.status.value.isLoading)
        verify(exactly = 0) { sessionManager.saveUserId(any()) }
        verify(exactly = 0) { syncManager.scheduleSync() }
    }

    @Test
    fun `clearEvent vuelve el evento a None`() = runTest {
        coEvery { loginUseCase(any()) } returns flowOf(
            Result.Success(LoginUIModel("Bienvenido", 1L))
        )
        viewModel.onLoginChanged("tecnico@tradejob.es", "1234567")
        viewModel.login()
        assertEquals(Event.Success("Bienvenido"), viewModel.event.value)

        viewModel.clearEvent()

        assertEquals(Event.None, viewModel.event.value)
    }
}
