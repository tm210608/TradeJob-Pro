@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.login.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tradejob.pro.components.MitoEmailField
import com.tradejob.pro.components.MitoPasswordField
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.components.resources.PrimaryColor
import com.tradejob.pro.components.resources.PrimaryDarkColor
import com.tradejob.pro.core.navigation.Screen

class LoginScreen : Screen {
    override val route: String = "login"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: LoginViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val status by viewModel.status.collectAsState()
        val event by viewModel.event.collectAsState()

        BackHandler {
            viewModel.showCloseDialog()
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                LoginContent(
                    status = status,
                    event = event,
                    onLoginClick = { viewModel.login() },
                    onLoginChanged = { email, password -> viewModel.onLoginChanged(email, password) },
                    onRegisterClick = { navController.navigate("register") },
                    onEventHandled = { viewModel.clearEvent() },
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun LoginContent(
    status: Status,
    event: Event,
    onLoginClick: () -> Unit,
    onLoginChanged: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onEventHandled: () -> Unit,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryDarkColor, PrimaryColor)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo Section
            Card(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = "TradeJob Pro Logo",
                        modifier = Modifier.size(80.dp),
                        tint = PrimaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TradeJob Pro",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Gestión profesional para autónomos",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Login Card
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
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )

                    MitoEmailField(
                        value = status.email,
                        onValueChange = { onLoginChanged(it, status.password) },
                        label = "Email",
                        placeholder = "tu@email.com",
                        modifier = Modifier.fillMaxWidth()
                    )

                    MitoPasswordField(
                        value = status.password,
                        onValueChange = { onLoginChanged(status.email, it) },
                        label = "Contraseña",
                        placeholder = "Tu contraseña",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PrimaryButton(
                        text = "ENTRAR",
                        onClick = onLoginClick,
                        enabled = status.loginEnable && !status.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿No tienes cuenta? Regístrate aquí",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Loading
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

    // Manejo de eventos
    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is Event.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                onEventHandled()
            }
            is Event.Success -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                // Navegar a home
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
                onEventHandled()
            }
            else -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginContent(
            status = Status(email = "test@example.com"),
            event = Event.None,
            onLoginClick = {},
            onLoginChanged = { _, _ -> },
            onRegisterClick = {},
            onEventHandled = {},
            navController = rememberNavController(),
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
