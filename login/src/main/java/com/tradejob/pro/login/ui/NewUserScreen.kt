@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tradejob.pro.components.MitoEmailField
import com.tradejob.pro.components.MitoPasswordField
import com.tradejob.pro.components.MitoTextField
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.components.resources.PrimaryColor
import com.tradejob.pro.components.resources.PrimaryDarkColor
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.components.theme.SystemBarIconStyle

import androidx.compose.ui.res.stringResource
import com.tradejob.pro.login.R

class NewUserScreen : Screen {
    override val route: String = "register"

    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: RegisterViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.register_title), color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.nav_back_login), tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryDarkColor
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) {
            Box {
                RegisterContent(
                    viewModel = viewModel,
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun RegisterContent(
    viewModel: RegisterViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val status by viewModel.status.collectAsState()
    val event by viewModel.event.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryDarkColor, PrimaryColor)
                )
            )
    ) {
        SystemBarIconStyle(darkIcons = false, navigationBarColor = PrimaryColor)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.register_title_main),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MitoTextField(
                        value = status.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        label = stringResource(R.string.register_label_name),
                        placeholder = "Tu nombre",
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Nombre")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoEmailField(
                        value = status.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = stringResource(R.string.register_label_email),
                        placeholder = "tu@email.com",
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoPasswordField(
                        value = status.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        label = stringResource(R.string.register_label_password),
                        placeholder = "Mínimo 6 caracteres",
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoPasswordField(
                        value = status.confirmPassword,
                        onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                        label = stringResource(R.string.register_label_confirm_password),
                        placeholder = "Repite tu contraseña",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PrimaryButton(
                        text = stringResource(R.string.register_btn_create),
                        onClick = { viewModel.register() },
                        enabled = status.isValid && !status.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text(
                    text = stringResource(R.string.register_btn_back),
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (status.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    // Event handling
    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is RegisterEvent.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            is RegisterEvent.Success -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                navController.popBackStack()
                viewModel.clearEvent()
            }
            else -> {}
        }
    }
}
