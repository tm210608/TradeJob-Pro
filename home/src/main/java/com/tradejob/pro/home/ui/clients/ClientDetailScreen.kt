package com.tradejob.pro.home.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.home.ui.jobs.JobCard
import com.tradejob.pro.home.ui.jobs.JobStatus
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.PaddingValues
import com.tradejob.pro.components.MitoBottomSheet
import com.tradejob.pro.components.MitoButtonSheet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items as lazyItems

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
        val client by viewModel.client.collectAsState()
        val jobs by viewModel.jobs.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val event by viewModel.event.collectAsState()
        val selectedFilter by viewModel.selectedFilter.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current

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
                    val sendIntent: android.content.Intent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, currentEvent.text)
                        type = "text/plain"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                    viewModel.clearEvent()
                }
                else -> {}
            }
        }

        var jobToDelete by remember { mutableStateOf<com.tradejob.pro.database.data.entity.JobEntity?>(null) }

        if (jobToDelete != null) {
            MitoBottomSheet(
                mitoButtonSheet = MitoButtonSheet.ConfirmMitoButtonSheet(
                    title = "Eliminar trabajo",
                    message = "¿Estás seguro de que deseas eliminar este trabajo? Esta acción no se puede deshacer.",
                    confirmText = "Eliminar",
                    dismissText = "Cancelar",
                    onConfirm = {
                        jobToDelete?.let { viewModel.deleteJob(it) }
                        jobToDelete = null
                    },
                    onDismiss = { jobToDelete = null }
                )
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle del Cliente") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        client?.let {
                            IconButton(onClick = { navController.navigate("client_form/${it.id}") }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar cliente")
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                if (isLoading && client == null) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    client?.let {
                        ClientDetailContent(
                            client = it,
                            jobs = jobs,
                            selectedFilter = selectedFilter,
                            onFilterSelected = { filter -> viewModel.onFilterSelected(filter) },
                            onDeleteJob = { job -> jobToDelete = job },
                            onExportJob = { job -> viewModel.exportJob(job) },
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
    jobs: List<com.tradejob.pro.database.data.entity.JobEntity>,
    selectedFilter: JobStatus?,
    onFilterSelected: (JobStatus?) -> Unit,
    onDeleteJob: (com.tradejob.pro.database.data.entity.JobEntity) -> Unit,
    onExportJob: (com.tradejob.pro.database.data.entity.JobEntity) -> Unit,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Información del Cliente
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                
                if (!client.notes.isNullOrBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(text = "Notas:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = client.notes ?: "", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Sección de Trabajos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trabajos / Averías",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedButton(
                onClick = { navController.navigate("job_form/${client.id}/0") },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Nuevo")
            }
        }

        // Filtros de estado
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { onFilterSelected(null) },
                    label = { Text("Todos") }
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
                    Text(text = "Sin trabajos aún", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            jobs.forEach { job ->
                JobCard(
                    job = job,
                    onClick = { navController.navigate("job_form/${job.clientId}/${job.id}") },
                    onDelete = { onDeleteJob(job) },
                    onExport = { onExportJob(job) },
                    onExportPdf = { /* No implementado aquí para simplicidad */ }
                )
            }
        }
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
