package com.tradejob.pro.home.ui.jobs

import androidx.lifecycle.SavedStateHandle
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import com.tradejob.pro.home.domain.AddJobPhotoUseCase
import com.tradejob.pro.home.domain.DeleteJobPhotoUseCase
import com.tradejob.pro.home.domain.GetJobByIdUseCase
import com.tradejob.pro.home.domain.GetPhotosByJobUseCase
import com.tradejob.pro.home.domain.SaveJobUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobFormViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val saveJobUseCase: SaveJobUseCase = mockk()
    private val getJobByIdUseCase: GetJobByIdUseCase = mockk()
    private val getPhotosByJobUseCase: GetPhotosByJobUseCase = mockk()
    private val addJobPhotoUseCase: AddJobPhotoUseCase = mockk()
    private val deleteJobPhotoUseCase: DeleteJobPhotoUseCase = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()

    private lateinit var viewModel: JobFormViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when viewmodel starts without jobId then is in creation mode`() = runTest {
        // Given
        every { savedStateHandle.get<String>("clientId") } returns "1"
        every { savedStateHandle.get<String>("jobId") } returns null

        // When
        viewModel = JobFormViewModel(
            saveJobUseCase,
            getJobByIdUseCase,
            getPhotosByJobUseCase,
            addJobPhotoUseCase,
            deleteJobPhotoUseCase,
            savedStateHandle
        )

        // Then
        assertFalse(viewModel.isEditMode)
        assertEquals("", viewModel.title.value)
    }

    @Test
    fun `when viewmodel starts with jobId then loads job data`() = runTest {
        // Given
        val jobId = 10L
        val job = JobEntity(id = jobId, clientId = 1L, title = "Original Title", description = "Desc")
        every { savedStateHandle.get<String>("clientId") } returns "1"
        every { savedStateHandle.get<String>("jobId") } returns jobId.toString()
        coEvery { getJobByIdUseCase(jobId) } returns Result.Success(job)
        every { getPhotosByJobUseCase(jobId) } returns flowOf(Result.Success(emptyList()))

        // When
        viewModel = JobFormViewModel(
            saveJobUseCase,
            getJobByIdUseCase,
            getPhotosByJobUseCase,
            addJobPhotoUseCase,
            deleteJobPhotoUseCase,
            savedStateHandle
        )

        // Then
        assertTrue(viewModel.isEditMode)
        assertEquals("Original Title", viewModel.title.value)
        assertEquals("Desc", viewModel.description.value)
    }

    @Test
    fun `when title changes then state is updated`() = runTest {
        // Given
        every { savedStateHandle.get<String>("clientId") } returns "1"
        every { savedStateHandle.get<String>("jobId") } returns null
        viewModel = JobFormViewModel(
            saveJobUseCase,
            getJobByIdUseCase,
            getPhotosByJobUseCase,
            addJobPhotoUseCase,
            deleteJobPhotoUseCase,
            savedStateHandle
        )

        // When
        viewModel.onTitleChanged("New Job")

        // Then
        assertEquals("New Job", viewModel.title.value)
    }

    @Test
    fun `when add photo in creation mode then photo is added to temp list`() = runTest {
        // Given
        every { savedStateHandle.get<String>("clientId") } returns "1"
        every { savedStateHandle.get<String>("jobId") } returns null
        viewModel = JobFormViewModel(
            saveJobUseCase,
            getJobByIdUseCase,
            getPhotosByJobUseCase,
            addJobPhotoUseCase,
            deleteJobPhotoUseCase,
            savedStateHandle
        )

        // When
        viewModel.addPhoto("path/to/photo.jpg")

        // Then
        assertEquals(1, viewModel.photos.value.size)
        assertEquals("path/to/photo.jpg", viewModel.photos.value[0].photoPath)
    }

    @Test
    fun `when save with blank title then error event is emitted`() = runTest {
        // Given
        every { savedStateHandle.get<String>("clientId") } returns "1"
        every { savedStateHandle.get<String>("jobId") } returns null
        viewModel = JobFormViewModel(
            saveJobUseCase,
            getJobByIdUseCase,
            getPhotosByJobUseCase,
            addJobPhotoUseCase,
            deleteJobPhotoUseCase,
            savedStateHandle
        )
        viewModel.onTitleChanged("")

        // When
        viewModel.save()

        // Then
        assertTrue(viewModel.event.value is JobFormEvent.Error)
        assertEquals(
            "Por favor, corrija los errores en el formulario",
            (viewModel.event.value as JobFormEvent.Error).message
        )
        assertTrue(viewModel.titleError.value is com.tradejob.pro.common.validation.ValidationResult.Error)
    }

    @Test
    fun `when save successful then success event is emitted`() = runTest {
        // Given
        every { savedStateHandle.get<String>("clientId") } returns "1"
        every { savedStateHandle.get<String>("jobId") } returns null
        coEvery { saveJobUseCase(any(), true) } returns Result.Success(100L)
        
        viewModel = JobFormViewModel(
            saveJobUseCase,
            getJobByIdUseCase,
            getPhotosByJobUseCase,
            addJobPhotoUseCase,
            deleteJobPhotoUseCase,
            savedStateHandle
        )
        viewModel.onTitleChanged("Valid Job")

        // When
        viewModel.save()

        // Then
        assertTrue(viewModel.event.value is JobFormEvent.Success)
        assertEquals("Trabajo creado", (viewModel.event.value as JobFormEvent.Success).message)
    }
}
