package com.tradejob.pro.home.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tradejob.pro.components.MitoTextField
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.core.navigation.Screen

import androidx.compose.ui.res.stringResource
import com.tradejob.pro.home.R

class ClientFormScreen : Screen {
    override val route: String = "client_form/{clientId}"
    
    override val arguments = listOf(
        navArgument("clientId") {
            type = NavType.StringType
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: ClientFormViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(if (viewModel.isEditMode) stringResource(R.string.client_form_edit) else stringResource(R.string.client_form_new))
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            ClientFormContent(
                viewModel = viewModel,
                navController = navController,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun ClientFormContent(
    viewModel: ClientFormViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val name by viewModel.name.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val email by viewModel.email.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val address by viewModel.address.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isValid by viewModel.isValid.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()

    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is ClientFormEvent.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            is ClientFormEvent.Success -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                navController.popBackStack()
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

    if (isLoading && viewModel.isEditMode) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.client_form_section_data),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Nombre (obligatorio)
        MitoTextField(
            value = name,
            onValueChange = { viewModel.onNameChanged(it) },
            label = stringResource(R.string.client_label_name),
            placeholder = stringResource(R.string.client_placeholder_name),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        // Teléfono (obligatorio)
        MitoTextField(
            value = phone,
            onValueChange = { viewModel.onPhoneChanged(it) },
            label = stringResource(R.string.client_label_phone),
            placeholder = stringResource(R.string.client_placeholder_phone),
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        // Email (opcional)
        MitoTextField(
            value = email,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = stringResource(R.string.client_label_email),
            placeholder = stringResource(R.string.client_placeholder_email),
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
            errorText = emailError,
            modifier = Modifier.fillMaxWidth()
        )

        // Dirección (opcional)
        MitoTextField(
            value = address,
            onValueChange = { viewModel.onAddressChanged(it) },
            label = stringResource(R.string.client_label_address),
            placeholder = stringResource(R.string.client_placeholder_address),
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        // Notas (opcional)
        MitoTextField(
            value = notes,
            onValueChange = { viewModel.onNotesChanged(it) },
            label = stringResource(R.string.client_label_notes),
            placeholder = stringResource(R.string.client_placeholder_notes),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = if (viewModel.isEditMode) stringResource(R.string.client_btn_save) else stringResource(R.string.client_btn_create),
            onClick = { viewModel.save() },
            enabled = isValid && !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
