package com.tradejob.pro.home.ui.jobs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tradejob.pro.components.MitoBottomSheet
import com.tradejob.pro.components.MitoButtonSheet
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.components.theme.AmberWarning
import com.tradejob.pro.components.theme.AmberWarningDark
import com.tradejob.pro.components.theme.BoneCard
import com.tradejob.pro.components.theme.BoneLight
import com.tradejob.pro.components.theme.Petrol20
import com.tradejob.pro.components.theme.Petrol30
import com.tradejob.pro.components.theme.SystemBarIconStyle
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.R
import java.io.File

class JobListScreen : Screen {
    override val route: String = "job_list/{clientId}"

    override val arguments = listOf(
        navArgument("clientId") {
            type = NavType.StringType
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: JobListViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val clientName by viewModel.clientName.collectAsState()
        val darkTheme = isSystemInDarkTheme()
        val clientId = navController.currentBackStackEntry?.arguments?.getString("clientId") ?: "0"

        SystemBarIconStyle(
            darkIcons = !darkTheme,
            navigationBarColor = if (darkTheme) Petrol30 else BoneLight
        )

        val backgroundModifier = if (darkTheme) {
            Modifier.background(brush = Brush.verticalGradient(colors = listOf(Petrol20, Petrol30)))
        } else {
            Modifier.background(BoneLight)
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = clientName ?: stringResource(R.string.job_list_title_fallback),
                            fontWeight = FontWeight.Bold,
                            color = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.nav_back),
                                tint = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("job_form/$clientId/0") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.job_form_new))
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().then(backgroundModifier)) {
                JobListContent(
                    viewModel = viewModel,
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    clientId = clientId,
                    darkTheme = darkTheme,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListContent(
    viewModel: JobListViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    clientId: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.jobs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    var jobToDelete by remember { mutableStateOf<JobEntity?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is JobListEvent.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            is JobListEvent.Success -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            is JobListEvent.Export -> {
                val sendIntent: android.content.Intent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, currentEvent.text)
                    type = "text/plain"
                }
                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
                viewModel.clearEvent()
            }
            is JobListEvent.ExportPdf -> {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    currentEvent.file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    android.content.Intent.createChooser(intent, context.getString(R.string.job_share_pdf))
                )
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

    if (jobToDelete != null) {
        MitoBottomSheet(
            mitoButtonSheet = MitoButtonSheet.ConfirmMitoButtonSheet(
                title = stringResource(R.string.job_delete_title),
                message = stringResource(R.string.job_delete_message),
                confirmText = stringResource(R.string.job_delete_confirm),
                dismissText = stringResource(R.string.job_delete_cancel),
                onConfirm = {
                    jobToDelete?.let { viewModel.deleteJob(it) }
                    jobToDelete = null
                },
                onDismiss = { jobToDelete = null }
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.onFilterSelected(null) },
                    label = { Text(stringResource(R.string.job_filter_all)) },
                    leadingIcon = if (selectedFilter == null) {
                        { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

            JobStatus.entries.forEach { status ->
                item {
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { viewModel.onFilterSelected(status) },
                        label = { Text(status.displayName) }
                    )
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                jobs.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.height(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedFilter == null) {
                                stringResource(R.string.job_empty_all)
                            } else {
                                stringResource(R.string.job_empty_filtered)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (darkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
                        )
                        if (selectedFilter == null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(
                                text = stringResource(R.string.job_empty_cta),
                                onClick = { navController.navigate("job_form/$clientId/0") }
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(jobs, key = { it.id }) { job ->
                            JobCard(
                                job = job,
                                onClick = {
                                    navController.navigate("job_form/${job.clientId}/${job.id}")
                                },
                                onDelete = { jobToDelete = job },
                                onExport = { viewModel.exportJob(job) },
                                onExportPdf = { viewModel.exportJobPdf(context, job) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCard(
    job: JobEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onExportPdf: () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val status = JobStatus.fromValue(job.status)
    val priority = JobPriority.fromValue(job.priority)
    val statusColor = when (status) {
        JobStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
        JobStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        JobStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        JobStatus.INVOICED -> MaterialTheme.colorScheme.secondary
    }
    val urgentColor = if (darkTheme) AmberWarningDark else AmberWarning
    val contentColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val subtleColor = if (darkTheme) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant

    val date = formatJobDate(job.scheduledAt)
    val amount = formatJobAmount(job.finalAmount, job.budgetAmount)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (darkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surface else BoneCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = status.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        if (isUrgent(priority)) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(urgentColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.job_urgent),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = urgentColor
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onExportPdf) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.job_cd_pdf), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.job_cd_share), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.job_cd_delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            val infoLine = listOfNotNull(
                date ?: stringResource(R.string.job_no_date),
                amount
            ).joinToString("  ·  ")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = infoLine,
                style = MaterialTheme.typography.bodySmall,
                color = subtleColor
            )

            if (!job.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = job.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = subtleColor
                )
            }
        }
    }
}
