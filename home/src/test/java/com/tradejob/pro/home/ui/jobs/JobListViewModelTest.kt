package com.tradejob.pro.home.ui.jobs

import androidx.lifecycle.SavedStateHandle
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.DeleteJobUseCase
import com.tradejob.pro.home.domain.ExportJobUseCase
import com.tradejob.pro.home.domain.GeneratePdfReportUseCase
import com.tradejob.pro.home.domain.GetJobsByClientUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getJobsByClientUseCase: GetJobsByClientUseCase = mockk()
    private val deleteJobUseCase: DeleteJobUseCase = mockk()
    private val exportJobUseCase: ExportJobUseCase = mockk()
    private val generatePdfReportUseCase: GeneratePdfReportUseCase = mockk()
    private val clientRepository: ClientRepository = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()

    private lateinit var viewModel: JobListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<String>("clientId") } returns "1"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when viewmodel starts then loads jobs for client`() = runTest {
        // Given
        val jobs = listOf(JobEntity(id = 1, clientId = 1, title = "Job 1"))
        val jobsFlow = MutableStateFlow<Result<List<JobEntity>>>(Result.Success(jobs))
        every { getJobsByClientUseCase(1L) } returns jobsFlow

        // When
        viewModel = JobListViewModel(
            getJobsByClientUseCase,
            deleteJobUseCase,
            exportJobUseCase,
            generatePdfReportUseCase,
            clientRepository,
            savedStateHandle
        )
        
        // Start collection to trigger stateIn AND WAIT for a small delay or use Unconfined
        val collectionJob = launch(testDispatcher) { viewModel.jobs.collect {} }

        // Then
        assertEquals(jobs, viewModel.jobs.value)
        collectionJob.cancel()
    }

    @Test
    fun `when filter is applied then jobs are filtered`() = runTest {
        // Given
        val jobs = listOf(
            JobEntity(id = 1, clientId = 1, title = "Job 1", status = "PENDING"),
            JobEntity(id = 2, clientId = 1, title = "Job 2", status = "COMPLETED")
        )
        val jobsFlow = MutableStateFlow<Result<List<JobEntity>>>(Result.Success(jobs))
        every { getJobsByClientUseCase(1L) } returns jobsFlow
        
        viewModel = JobListViewModel(
            getJobsByClientUseCase,
            deleteJobUseCase,
            exportJobUseCase,
            generatePdfReportUseCase,
            clientRepository,
            savedStateHandle
        )
        
        val collectionJob = launch(testDispatcher) { viewModel.jobs.collect {} }

        // When
        viewModel.onFilterSelected(JobStatus.COMPLETED)

        // Then
        assertEquals(1, viewModel.jobs.value.size)
        assertEquals("Job 2", viewModel.jobs.value[0].title)
        collectionJob.cancel()
    }
}
