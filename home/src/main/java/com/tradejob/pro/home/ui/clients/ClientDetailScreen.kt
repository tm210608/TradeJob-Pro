package com.tradejob.pro.home.ui.clients

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tradejob.pro.components.MitoBottomSheet
import com.tradejob.pro.components.MitoButtonSheet
import com.tradejob.pro.components.theme.BoneCard
import com.tradejob.pro.components.theme.BoneLight
import com.tradejob.pro.components.theme.Petrol20
import com.tradejob.pro.components.theme.Petrol30
import com.tradejob.pro.components.theme.SystemBarIconStyle
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.home.R
import com.tradejob.pro.home.ui.jobs.JobCard
import com.tradejob.pro.home.ui.jobs.JobStatus
import kotlinx.coroutines.launch

private fun launchExternal(context: Context, intent: Intent): Boolean = try {
    context.startActivity(intent)
    true
} catch (e: ActivityNotFoundException) {
    false
}

class ClientDetailScreen : Screen {
    override val route: String = "client_detail/{clientId}"

    override val arguments = listOf(
        navArgument("clientId") {
            type = NavType.StringType
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: ClientDetailViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val client by viewModel.client.collectAsState()
        val jobs by viewModel.jobs.collectAsState()
        val allJobs by viewModel.allJobs.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val event by viewModel.event.collectAsState()
        val selectedFilter by viewModel.selectedFilter.collectAsState()
        val context = LocalContext.current
        val darkTheme = isSystemInDarkTheme()
        val noAppMessage = stringResource(R.string.client_action_no_app)

        SystemBarIconStyle(
            darkIcons = !darkTheme,
            navigationBarColor = if (darkTheme) Petrol30 else BoneLight
        )

        LaunchedEffect(event) {
            when (val currentEvent = event) {
                is ClientDetailEvent.Error -> {
                    snackbarHostState.showSnackbar(currentEvent.message)
                    viewModel.clearEvent()
                }
                is ClientDetailEvent.Success -> {
                    snackbarHostState.showSnackbar(currentEvent.message)
                    viewModel.clearEvent()
                }
                is ClientDetailEvent.Export -> {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, currentEvent.text)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                    viewModel.clearEvent()
                }
                else -> {}
            }
        }

        var jobToDelete by remember { mutableStateOf<JobEntity?>(null) }

        if (jobToDelete != null) {
            MitoBottomSheet(
                mitoButtonSheet = MitoButtonSheet.ConfirmMitoButtonSheet(
                    title = stringResource(R.string.job_delete_title),
                    message = stringResource(R.string.job_delete_msg),
                    confirmText = stringResource(R.string.btn_delete),
                    dismissText = stringResource(R.string.btn_cancel),
                    onConfirm = {
                        jobToDelete?.let { viewModel.deleteJob(it) }
                        jobToDelete = null
                    },
                    onDismiss = { jobToDelete = null }
                )
            )
        }

        val backgroundModifier = if (darkTheme) {
            Modifier.background(brush = Brush.verticalGradient(colors = listOf(Petrol20, Petrol30)))
        } else {
            Modifier.background(BoneLight)
        }
        val topBarContentColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = client?.name ?: stringResource(R.string.nav_clients),
                            fontWeight = FontWeight.Bold,
                            color = topBarContentColor
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.nav_back),
                                tint = topBarContentColor
                            )
                        }
                    },
                    actions = {
                        client?.let {
                            IconButton(onClick = { navController.navigate("quote_list/${it.id}") }) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = stringResource(R.string.quote_nav_quotes),
                                    tint = topBarContentColor
                                )
                            }
                            IconButton(onClick = { navController.navigate("client_form/${it.id}") }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.client_form_edit),
                                    tint = topBarContentColor
                                )
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().then(backgroundModifier).padding(paddingValues)) {
                if (isLoading && client == null) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    client?.let {
                        ClientDetailContent(
                            client = it,
                            jobs = jobs,
                            allJobs = allJobs,
                            selectedFilter = selectedFilter,
                            darkTheme = darkTheme,
                            onFilterSelected = { filter -> viewModel.onFilterSelected(filter) },
                            onDeleteJob = { job -> jobToDelete = job },
                            onExportJob = { job -> viewModel.exportJob(job) },
                            onContactAction = { intent ->
                                if (!launchExternal(context, intent)) {
                                    scope.launch { snackbarHostState.showSnackbar(noAppMessage) }
                                }
                            },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailContent(
    client: ClientEntity,
    jobs: List<JobEntity>,
    allJobs: List<JobEntity>,
    selectedFilter: JobStatus?,
    darkTheme: Boolean,
    onFilterSelected: (JobStatus?) -> Unit,
    onDeleteJob: (JobEntity) -> Unit,
    onExportJob: (JobEntity) -> Unit,
    onContactAction: (Intent) -> Unit,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ClientCard(client = client, darkTheme = darkTheme, onContactAction = onContactAction)

        val counts = countJobsByStatus(allJobs)
        if (allJobs.isNotEmpty()) {
            JobsSummaryLine(counts = counts)
            lastJob(allJobs)?.let { last ->
                Text(
                    text = stringResource(R.string.client_last_job),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                JobCard(
                    job = last,
                    onClick = { navController.navigate("job_form/${last.clientId}/${last.id}") },
                    onDelete = { onDeleteJob(last) },
                    onExport = { onExportJob(last) },
                    onExportPdf = { }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.client_jobs_section),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = { navController.navigate("job_form/${client.id}/0") },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text(stringResource(R.string.job_form_new))
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { onFilterSelected(null) },
                    label = { Text(stringResource(R.string.job_filter_all)) }
                )
            }
            JobStatus.entries.forEach { status ->
                item {
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { onFilterSelected(status) },
                        label = { Text(status.displayName) }
                    )
                }
            }
        }

        if (jobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    Text(text = stringResource(R.string.job_empty_all), color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            jobs.forEach { job ->
                JobCard(
                    job = job,
                    onClick = { navController.navigate("job_form/${job.clientId}/${job.id}") },
                    onDelete = { onDeleteJob(job) },
                    onExport = { onExportJob(job) },
                    onExportPdf = { }
                )
            }
        }
    }
}

@Composable
fun ClientCard(
    client: ClientEntity,
    darkTheme: Boolean,
    onContactAction: (Intent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surface else BoneCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = client.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            InfoRow(icon = Icons.Default.Phone, text = client.phone)
            client.email?.takeIf { it.isNotBlank() }?.let { InfoRow(icon = Icons.Default.Email, text = it) }
            client.address?.takeIf { it.isNotBlank() }?.let { InfoRow(icon = Icons.Default.Home, text = it) }

            ContactActionsRow(client = client, onContactAction = onContactAction)

            if (!client.notes.isNullOrBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = stringResource(R.string.client_label_notes) + ":",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = client.notes ?: "", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ContactActionsRow(client: ClientEntity, onContactAction: (Intent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (client.phone.isNotBlank()) {
            ContactButton(
                icon = Icons.Default.Phone,
                label = stringResource(R.string.client_action_call),
                modifier = Modifier.weight(1f)
            ) {
                onContactAction(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}")))
            }
        }
        if (!client.email.isNullOrBlank()) {
            ContactButton(
                icon = Icons.Default.Email,
                label = stringResource(R.string.client_action_email),
                modifier = Modifier.weight(1f)
            ) {
                onContactAction(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.email}")))
            }
        }
        if (!client.address.isNullOrBlank()) {
            ContactButton(
                icon = Icons.Default.Place,
                label = stringResource(R.string.client_action_route),
                modifier = Modifier.weight(1f)
            ) {
                onContactAction(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(client.address)}")))
            }
        }
    }
}

@Composable
fun ContactButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.size(4.dp))
        Text(label, maxLines = 1)
    }
}

@Composable
fun JobsSummaryLine(counts: JobStatusCounts) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryItem(
            dotColor = MaterialTheme.colorScheme.primary,
            text = context.resources.getQuantityString(
                R.plurals.client_summary_in_progress, counts.inProgress, counts.inProgress
            )
        )
        SummaryItem(
            dotColor = MaterialTheme.colorScheme.onSurfaceVariant,
            text = context.resources.getQuantityString(
                R.plurals.client_summary_pending, counts.pending, counts.pending
            )
        )
        SummaryItem(
            dotColor = MaterialTheme.colorScheme.tertiary,
            text = context.resources.getQuantityString(
                R.plurals.client_summary_completed, counts.completed, counts.completed
            )
        )
    }
}

@Composable
fun SummaryItem(dotColor: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "●", color = dotColor, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
