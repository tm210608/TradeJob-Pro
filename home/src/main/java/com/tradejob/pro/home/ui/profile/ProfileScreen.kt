package com.tradejob.pro.home.ui.profile

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
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
import com.tradejob.pro.components.MitoTextField
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.core.navigation.Screen

import androidx.compose.ui.res.stringResource
import com.tradejob.pro.home.R

class ProfileScreen : Screen {
    override val route: String = "profile"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: ProfileViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val name by viewModel.name.collectAsState()
        val phone by viewModel.phone.collectAsState()
        val specialty by viewModel.specialty.collectAsState()
        val nif by viewModel.nif.collectAsState()
        val businessName by viewModel.businessName.collectAsState()
        val businessAddress by viewModel.businessAddress.collectAsState()
        val businessCity by viewModel.businessCity.collectAsState()
        val irpfRate by viewModel.irpfRate.collectAsState()
        val user by viewModel.user.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val event by viewModel.event.collectAsState()

        LaunchedEffect(event) {
            val currentEvent = event
            when (currentEvent) {
                is ProfileEvent.Error -> {
                    snackbarHostState.showSnackbar(currentEvent.message)
                    viewModel.clearEvent()
                }
                is ProfileEvent.Success -> {
                    snackbarHostState.showSnackbar(currentEvent.message)
                    viewModel.clearEvent()
                }
                else -> {}
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.profile_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            if (user == null && isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profile_section_pro),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    MitoTextField(
                        value = name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        label = stringResource(R.string.profile_label_name),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoTextField(
                        value = user?.email ?: "",
                        onValueChange = {},
                        label = stringResource(R.string.profile_label_email_readonly),
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoTextField(
                        value = phone,
                        onValueChange = { viewModel.onPhoneChanged(it) },
                        label = stringResource(R.string.profile_label_phone),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoTextField(
                        value = specialty,
                        onValueChange = { viewModel.onSpecialtyChanged(it) },
                        label = stringResource(R.string.profile_label_specialty),
                        placeholder = stringResource(R.string.profile_placeholder_specialty),
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.fiscal_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    MitoTextField(
                        value = nif,
                        onValueChange = { viewModel.onNifChanged(it) },
                        label = stringResource(R.string.fiscal_label_nif),
                        placeholder = stringResource(R.string.fiscal_placeholder_nif),
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoTextField(
                        value = businessName,
                        onValueChange = { viewModel.onBusinessNameChanged(it) },
                        label = stringResource(R.string.fiscal_label_business_name),
                        placeholder = stringResource(R.string.fiscal_placeholder_business_name),
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoTextField(
                        value = businessAddress,
                        onValueChange = { viewModel.onBusinessAddressChanged(it) },
                        label = stringResource(R.string.fiscal_label_address),
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoTextField(
                        value = businessCity,
                        onValueChange = { viewModel.onBusinessCityChanged(it) },
                        label = stringResource(R.string.fiscal_label_city),
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoTextField(
                        value = irpfRate,
                        onValueChange = { viewModel.onIrpfRateChanged(it) },
                        label = stringResource(R.string.fiscal_label_irpf),
                        placeholder = stringResource(R.string.fiscal_placeholder_irpf),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = stringResource(R.string.profile_btn_save),
                        onClick = { viewModel.saveProfile() },
                        enabled = !isLoading && name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
