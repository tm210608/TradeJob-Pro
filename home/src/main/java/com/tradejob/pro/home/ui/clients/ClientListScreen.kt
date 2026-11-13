package com.tradejob.pro.home.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tradejob.pro.components.MitoBottomSheet
import com.tradejob.pro.components.MitoButtonSheet
import com.tradejob.pro.components.MitoTextField
import com.tradejob.pro.components.theme.BoneCard
import com.tradejob.pro.components.theme.BoneLight
import com.tradejob.pro.components.theme.Petrol20
import com.tradejob.pro.components.theme.Petrol30
import com.tradejob.pro.components.theme.SystemBarIconStyle
import com.tradejob.pro.core.navigation.Screen

import androidx.compose.ui.res.stringResource
import com.tradejob.pro.home.R

class ClientListScreen : Screen {
    override val route: String = "client_list"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
override fun Content(navController: NavHostController) {
    val viewModel: ClientListViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearching by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val darkTheme = isSystemInDarkTheme()
    val topBarContentColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary

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
            if (isSearching) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text(stringResource(R.string.client_list_search)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearching = false
                            viewModel.onSearchQueryChanged("")
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.btn_cancel),
                                tint = topBarContentColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.client_list_title),
                            fontWeight = FontWeight.Bold,
                            color = topBarContentColor
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.nav_back),
                                tint = topBarContentColor
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.client_list_search),
                                tint = topBarContentColor
                            )
                        }
                    }
                )
            }
        },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("client_form/0") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.client_form_new))
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().then(backgroundModifier)) {
                ClientListContent(
                    viewModel = viewModel,
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    darkTheme = darkTheme,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ClientListContent(
    viewModel: ClientListViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val clients by viewModel.clients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()
    var clientToDelete by remember { mutableStateOf<com.tradejob.pro.database.data.entity.ClientEntity?>(null) }

    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is ClientListEvent.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            is ClientListEvent.Success -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

    if (clientToDelete != null) {
        MitoBottomSheet(
            mitoButtonSheet = MitoButtonSheet.ConfirmMitoButtonSheet(
                title = stringResource(R.string.client_delete_title),
                message = stringResource(R.string.client_delete_msg, clientToDelete?.name ?: ""),
                confirmText = stringResource(R.string.btn_delete),
                dismissText = stringResource(R.string.btn_cancel),
                onConfirm = {
                    clientToDelete?.let { viewModel.deleteClient(it) }
                    clientToDelete = null
                },
                onDismiss = { clientToDelete = null }
            )
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            clients.isEmpty() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.client_list_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = stringResource(R.string.client_list_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                ) {
                    items(clients, key = { it.id }) { client ->
                        ClientCard(
                            client = client,
                            darkTheme = darkTheme,
                            onEdit = { navController.navigate("client_form/${client.id}") },
                            onDelete = { clientToDelete = client },
                            onViewJobs = { navController.navigate("client_detail/${client.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClientCard(
    client: com.tradejob.pro.database.data.entity.ClientEntity,
    darkTheme: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewJobs: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onViewJobs,
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surface else BoneCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        text = client.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (client.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.height(16.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = client.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.client_form_edit))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete))
                    }
                }
            }
            val address = client.address
            if (!address.isNullOrBlank()) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.client_view_jobs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
