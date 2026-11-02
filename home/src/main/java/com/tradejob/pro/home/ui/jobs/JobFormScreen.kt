@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.home.ui.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.tradejob.pro.components.MitoNumberField
import com.tradejob.pro.components.MitoTextField
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import com.tradejob.pro.common.validation.ValidationResult
import com.tradejob.pro.components.extensions.asString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.io.File

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.stringResource
import com.tradejob.pro.common.camera.CameraUtils
import com.tradejob.pro.home.R

class JobFormScreen : Screen {
    override val route: String = "job_form/{clientId}/{jobId}"
    
    override val arguments = listOf(
        navArgument("clientId") {
            type = NavType.StringType
        },
        navArgument("jobId") {
            type = NavType.StringType
        }
    )

    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: JobFormViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val context = androidx.compose.ui.platform.LocalContext.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(if (viewModel.isEditMode) stringResource(R.string.job_form_edit) else stringResource(R.string.job_form_new))
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
            JobFormContent(
                viewModel = viewModel,
                navController = navController,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun PhotoItem(
    photo: JobPhotoEntity,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .padding(4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.photoPath,
                contentDescription = stringResource(R.string.job_photo_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_menu_report_image)
            )
            
            Surface(
                color = if (photo.type == "BEFORE") Color.Red.copy(alpha = 0.7f) else Color.Green.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .clip(CircleShape)
            ) {
                Text(
                    text = if (photo.type == "BEFORE") "Antes" else "Después",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.job_photo_delete),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FullScreenPhotoViewer(
    photoPath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = photoPath,
                contentDescription = stringResource(R.string.job_photo_fullview),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.job_photo_close),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun JobFormContent(
    viewModel: JobFormViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val title by viewModel.title.collectAsState()
    val titleError by viewModel.titleError.collectAsState()
    val description by viewModel.description.collectAsState()
    val status by viewModel.status.collectAsState()
    val priority by viewModel.priority.collectAsState()
    val budgetAmount by viewModel.budgetAmount.collectAsState()
    val budgetError by viewModel.budgetError.collectAsState()
    val finalAmount by viewModel.finalAmount.collectAsState()
    val finalAmountError by viewModel.finalAmountError.collectAsState()
    val scheduledAt by viewModel.scheduledAt.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedPhotoPath by remember { mutableStateOf<String?>(null) }
    var showPhotoTypeDialog by remember { mutableStateOf(false) }
    var pendingPhotoType by remember { mutableStateOf("BEFORE") }
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                val photoFile = File(uri.path ?: return@let)
                val compressedFile = CameraUtils.compressImage(photoFile)
                viewModel.addPhoto(compressedFile.absolutePath, pendingPhotoType)
            }
        }
        tempPhotoUri = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showPhotoTypeDialog = true
        }
    }

    if (showPhotoTypeDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoTypeDialog = false },
            title = { Text(stringResource(R.string.camera_photo_type_title)) },
            text = {
                Column {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.camera_photo_before)) },
                        onClick = {
                            pendingPhotoType = "BEFORE"
                            showPhotoTypeDialog = false
                            val photoFile = CameraUtils.createImageFile(context, viewModel.jobId ?: 0L)
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            tempPhotoUri = uri
                            cameraLauncher.launch(uri)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.camera_photo_after)) },
                        onClick = {
                            pendingPhotoType = "AFTER"
                            showPhotoTypeDialog = false
                            val photoFile = CameraUtils.createImageFile(context, viewModel.jobId ?: 0L)
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            tempPhotoUri = uri
                            cameraLauncher.launch(uri)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.camera_photo_progress)) },
                        onClick = {
                            pendingPhotoType = "PROGRESS"
                            showPhotoTypeDialog = false
                            val photoFile = CameraUtils.createImageFile(context, viewModel.jobId ?: 0L)
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            tempPhotoUri = uri
                            cameraLauncher.launch(uri)
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoTypeDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (selectedPhotoPath != null) {
        FullScreenPhotoViewer(
            photoPath = selectedPhotoPath!!,
            onDismiss = { selectedPhotoPath = null }
        )
    }

    LaunchedEffect(event) {
        val currentEvent = event
        when (currentEvent) {
            is JobFormEvent.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            is JobFormEvent.Success -> {
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
            text = stringResource(R.string.job_form_edit), // Reusing a title but should be more generic like "Detalles"
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        MitoTextField(
            value = title,
            onValueChange = { viewModel.onTitleChanged(it) },
            label = stringResource(R.string.job_label_title),
            placeholder = "Ej: Reparación de fuga en baño",
            errorText = titleError.asString(),
            leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MitoNumberField(
                value = budgetAmount,
                onValueChange = { viewModel.onBudgetAmountChanged(it) },
                label = stringResource(R.string.job_label_budget),
                placeholder = "0.00",
                errorText = budgetError.asString(),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
            
            MitoNumberField(
                value = finalAmount,
                onValueChange = { viewModel.onFinalAmountChanged(it) },
                label = stringResource(R.string.job_label_final_amount),
                placeholder = "0.00",
                errorText = finalAmountError.asString(),
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
        }

        MitoTextField(
            value = if (scheduledAt != null) dateFormatter.format(Date(scheduledAt!!)) else "",
            onValueChange = {},
            readOnly = true,
            label = stringResource(R.string.job_label_date),
            placeholder = "Seleccionar fecha",
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance()
                    scheduledAt?.let { calendar.timeInMillis = it }
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newCalendar = Calendar.getInstance()
                            newCalendar.set(year, month, dayOfMonth)
                            viewModel.onScheduledAtChanged(newCalendar.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
            enabled = true
        )

        MitoTextField(
            value = description,
            onValueChange = { viewModel.onDescriptionChanged(it) },
            label = stringResource(R.string.job_label_description),
            placeholder = "Detalles sobre el trabajo a realizar...",
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 5
        )

        // Estado
        var statusExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = statusExpanded,
            onExpandedChange = { statusExpanded = !statusExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            MitoTextField(
                value = status.displayName,
                onValueChange = {},
                readOnly = true,
                label = stringResource(R.string.job_label_status),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            DropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false }
            ) {
                JobStatus.entries.forEach { jobStatus ->
                    DropdownMenuItem(
                        text = { Text(jobStatus.displayName) },
                        onClick = {
                            viewModel.onStatusChanged(jobStatus)
                            statusExpanded = false
                        }
                    )
                }
            }
        }

        // Prioridad
        var priorityExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = priorityExpanded,
            onExpandedChange = { priorityExpanded = !priorityExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            MitoTextField(
                value = priority.displayName,
                onValueChange = {},
                readOnly = true,
                label = stringResource(R.string.job_label_priority),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            DropdownMenu(
                expanded = priorityExpanded,
                onDismissRequest = { priorityExpanded = false }
            ) {
                JobPriority.entries.forEach { jobPriority ->
                    DropdownMenuItem(
                        text = { Text(jobPriority.displayName) },
                        onClick = {
                            viewModel.onPriorityChanged(jobPriority)
                            priorityExpanded = false
                        }
                    )
                }
            }
        }

        // Fotos
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.job_section_photos),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            }) {
                Icon(Icons.Default.AddAPhoto, contentDescription = stringResource(R.string.job_btn_add_photo))
            }
        }

        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.job_photos_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos) { photo ->
                    PhotoItem(
                        photo = photo,
                        onDelete = { viewModel.deletePhoto(photo) },
                        onClick = { selectedPhotoPath = photo.photoPath }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = if (viewModel.isEditMode) stringResource(R.string.client_btn_save) else stringResource(R.string.btn_save),
            onClick = { viewModel.save() },
            enabled = !isLoading,
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
