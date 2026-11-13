package com.tradejob.pro.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tradejob.pro.common.ui.greetingForHour
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.components.MitoSignatureDivider
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.components.theme.BoneCard
import com.tradejob.pro.components.theme.BoneLight
import com.tradejob.pro.components.theme.Petrol20
import com.tradejob.pro.components.theme.Petrol30
import com.tradejob.pro.components.theme.SystemBarIconStyle
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.home.R
import com.tradejob.pro.home.domain.HomeStats
import com.tradejob.pro.home.ui.jobs.JobStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeScreen : Screen {
    override val route: String = "home"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: HomeViewModel = hiltViewModel()
        val userName by viewModel.userName.collectAsState()
        val menuExpanded by viewModel.menuExpanded.collectAsState()
        val statsResult by viewModel.stats.collectAsState()
        val darkTheme = isSystemInDarkTheme()

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
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            color = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.onMenuClick() }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.nav_home),
                                tint = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { viewModel.dismissMenu() }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_clients)) },
                                onClick = {
                                    viewModel.dismissMenu()
                                    navController.navigate("client_list")
                                },
                                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_profile)) },
                                onClick = {
                                    viewModel.dismissMenu()
                                    navController.navigate("profile")
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.invoice_nav_invoices)) },
                                onClick = {
                                    viewModel.dismissMenu()
                                    navController.navigate("invoice_list")
                                },
                                leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_logout)) },
                                onClick = {
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) }
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .then(backgroundModifier)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Cabecera del parte: fecha real + saludo con nombre.
                Text(
                    text = SimpleDateFormat(
                        "EEEE d 'de' MMMM",
                        Locale.forLanguageTag("es-ES")
                    ).format(Date()).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = if (darkTheme) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.home_greeting_name,
                        stringResource(greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))),
                        userName
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                when (val result = statsResult) {
                    is Result.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is Result.Success -> {
                        val stats = result.data
                        if (stats.isEmpty) {
                            EmptyHomeCard(
                                darkTheme = darkTheme,
                                onCreateClient = { navController.navigate("client_form/0") }
                            )
                        } else {
                            HomeContent(
                                stats = stats,
                                darkTheme = darkTheme,
                                navController = navController
                            )
                        }
                    }
                    is Result.Error -> {
                        Text(
                            text = stringResource(R.string.home_error_stats),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.home_version),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = if (darkTheme) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HomeContent(
    stats: HomeStats,
    darkTheme: Boolean,
    navController: NavHostController
) {
    ParteCard(stats = stats, darkTheme = darkTheme)

    if (stats.recentJobs.isNotEmpty()) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.home_recent_jobs).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = if (darkTheme) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        stats.recentJobs.forEach { job ->
            JobLine(
                job = job,
                darkTheme = darkTheme,
                onClick = { navController.navigate("job_form/${job.clientId}/${job.id}") }
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
    Text(
        text = stringResource(R.string.home_quick_actions).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        color = if (darkTheme) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickActionCard(
            title = stringResource(R.string.home_new_client),
            icon = Icons.Default.Group,
            darkTheme = darkTheme,
            onClick = { navController.navigate("client_form/0") },
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = stringResource(R.string.home_view_agenda),
            icon = Icons.Default.PendingActions,
            darkTheme = darkTheme,
            onClick = { navController.navigate("client_list") },
            modifier = Modifier.weight(1f)
        )
    }
}

// El parte de la semana: una sola tarjeta-formulario con borde, líneas,
// pie de totales y la firma que "cierra" la jornada.
@Composable
private fun ParteCard(stats: HomeStats, darkTheme: Boolean) {
    val es = Locale.forLanguageTag("es-ES")
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
    }
    val weekStart = SimpleDateFormat("d", es).format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, 6)
    val weekEnd = SimpleDateFormat("d 'de' MMMM", es).format(cal.time)

    val contentColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val subtleColor = if (darkTheme) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (darkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surface else BoneCard
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_parte_title).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.home_week_range, weekStart, weekEnd),
                    style = MaterialTheme.typography.labelMedium,
                    color = subtleColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stats.jobsThisWeek.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (darkTheme) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = pluralStringResource(R.plurals.home_jobs_unit, stats.jobsThisWeek, stats.jobsThisWeek),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (stats.completedThisWeek > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                ParteCheckLine(
                    text = pluralStringResource(
                        R.plurals.home_celebration_jobs,
                        stats.completedThisWeek, stats.completedThisWeek
                    )
                )
            }
            if (stats.paidInvoicesThisWeek > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                ParteCheckLine(
                    text = pluralStringResource(
                        R.plurals.home_celebration_invoices,
                        stats.paidInvoicesThisWeek, stats.paidInvoicesThisWeek
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            DashedDivider(color = subtleColor)
            Spacer(modifier = Modifier.height(12.dp))

            val footer = buildList {
                add(pluralStringResource(R.plurals.home_count_pending, stats.pendingJobs, stats.pendingJobs))
                add(stringResource(R.string.home_count_progress, stats.inProgressJobs))
                add(pluralStringResource(R.plurals.home_count_done, stats.completedJobs, stats.completedJobs))
            }.joinToString("  ·  ")
            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = subtleColor
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                MitoSignatureDivider(width = 110.dp, height = 26.dp)
            }
        }
    }
}

@Composable
private fun ParteCheckLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun DashedDivider(color: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        )
    }
}

// Línea de trabajo dentro del parte: punto de estado + título + estado.
@Composable
private fun JobLine(job: JobEntity, darkTheme: Boolean, onClick: () -> Unit) {
    val status = JobStatus.fromValue(job.status)
    val statusColor = when (status) {
        JobStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
        JobStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        JobStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        JobStatus.INVOICED -> MaterialTheme.colorScheme.secondary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(statusColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = job.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor
            )
        }
    }
}

@Composable
private fun EmptyHomeCard(darkTheme: Boolean, onCreateClient: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surface else BoneCard
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = if (darkTheme) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(R.string.home_empty_cta),
                onClick = onCreateClient
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    darkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surface else BoneCard
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
