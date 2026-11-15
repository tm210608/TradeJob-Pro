@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tradejob.pro.home.ui.quotes

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.tradejob.pro.components.MitoNumberField
import com.tradejob.pro.components.MitoTextField
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.components.SignaturePad
import com.tradejob.pro.components.theme.BoneCard
import com.tradejob.pro.components.theme.BoneLight
import com.tradejob.pro.components.theme.Petrol20
import com.tradejob.pro.components.theme.Petrol30
import com.tradejob.pro.components.theme.SystemBarIconStyle
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.home.R

class QuoteFormScreen : Screen {
    override val route: String = "quote_form/{clientId}/{quoteId}"

    override val arguments = listOf(
        navArgument("clientId") {
            type = NavType.StringType
        },
        navArgument("quoteId") {
            type = NavType.StringType
        }
    )

    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: QuoteFormViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val darkTheme = isSystemInDarkTheme()
        var showDeleteConfirm by remember { mutableStateOf(false) }

        SystemBarIconStyle(
            darkIcons = !darkTheme,
            navigationBarColor = if (darkTheme) Petrol30 else BoneLight
        )

        val backgroundModifier = if (darkTheme) {
            Modifier.background(brush = Brush.verticalGradient(colors = listOf(Petrol20, Petrol30)))
        } else {
            Modifier.background(BoneLight)
        }
        val topBarContentColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary

        if (showDeleteConfirm) {
            MitoBottomSheet(
                mitoButtonSheet = MitoButtonSheet.ConfirmMitoButtonSheet(
                    title = stringResource(R.string.quote_delete_title),
                    message = stringResource(R.string.quote_delete_msg),
                    confirmText = stringResource(R.string.btn_delete),
                    dismissText = stringResource(R.string.btn_cancel),
                    onConfirm = {
                        viewModel.delete()
                        showDeleteConfirm = false
                    },
                    onDismiss = { showDeleteConfirm = false }
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (viewModel.isEditMode) {
                                stringResource(R.string.quote_form_edit)
                            } else {
                                stringResource(R.string.quote_form_new)
                            },
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
                        if (viewModel.isEditMode) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.btn_delete),
                                    tint = topBarContentColor
                                )
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().then(backgroundModifier)) {
                QuoteFormContent(
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
fun QuoteFormContent(
    viewModel: QuoteFormViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsState()
    val vatRate by viewModel.vatRate.collectAsState()
    val status by viewModel.status.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()
    val isConverting by viewModel.isConverting.collectAsState()
    val context = LocalContext.current

    val contentColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val subtleColor = if (darkTheme) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is QuoteFormEvent.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            is QuoteFormEvent.Success -> {
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

    var showAddItem by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.quote_label_status),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuoteStatus.entries.forEach { s ->
                FilterChip(
                    selected = status == s,
                    onClick = { viewModel.onStatusChanged(s) },
                    label = { Text(stringResource(s.displayRes)) }
                )
            }
        }

        Text(
            text = stringResource(R.string.quote_section_items),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.quote_empty_items),
                    color = subtleColor
                )
            }
        } else {
            items.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = contentColor,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.removeItem(index) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.btn_delete_item),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Text(
                            text = "${formatQuoteQuantity(item.quantity)} x ${formatQuoteAmount(item.unitPrice)} = ${formatQuoteAmount(item.total)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = subtleColor
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { showAddItem = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.quote_btn_add_item))
        }

        if (showAddItem) {
            AddItemDialog(
                onAdd = { desc, qty, price ->
                    viewModel.addItem(desc, qty, price)
                    showAddItem = false
                },
                onDismiss = { showAddItem = false }
            )
        }

        val subtotal = viewModel.calculateSubtotal()
        val vatAmount = viewModel.calculateVatAmount()
        val total = viewModel.calculateTotal()

        Card(
            modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.quote_label_subtotal)}: ${formatQuoteAmount(subtotal)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
                Text(
                    text = "${stringResource(R.string.quote_label_vat_amount)} (${vatRate.toInt()}%): ${formatQuoteAmount(vatAmount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
                Text(
                    text = "${stringResource(R.string.quote_label_total)}: ${formatQuoteAmount(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        MitoNumberField(
            value = vatRate.toString(),
            onValueChange = { viewModel.onVatRateChanged(it.toDoubleOrNull() ?: 21.0) },
            label = stringResource(R.string.quote_label_vat),
            modifier = Modifier.fillMaxWidth()
        )

        MitoTextField(
            value = notes,
            onValueChange = { viewModel.onNotesChanged(it) },
            label = stringResource(R.string.quote_label_notes),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3
        )

        var showSignatureDialog by remember { mutableStateOf(false) }
        var signaturePath by remember { mutableStateOf<String?>(null) }

        if (showSignatureDialog) {
            AlertDialog(
                onDismissRequest = { showSignatureDialog = false },
                title = { Text(stringResource(R.string.signature_dialog_title)) },
                text = {
                    SignaturePad(
                        context = LocalContext.current,
                        hintText = stringResource(R.string.signature_pad_hint),
                        clearText = stringResource(R.string.signature_btn_clear),
                        saveText = stringResource(R.string.signature_btn_save),
                        onSignatureSaved = { file ->
                            signaturePath = file.absolutePath
                            showSignatureDialog = false
                        }
                    )
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSignatureDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }

        if (signaturePath != null) {
            Text(
                text = stringResource(R.string.signature_saved),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedButton(
            onClick = { showSignatureDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (signaturePath != null) {
                    stringResource(R.string.signature_btn_change)
                } else {
                    stringResource(R.string.signature_btn_request)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = if (viewModel.isEditMode) stringResource(R.string.quote_btn_save) else stringResource(R.string.quote_btn_create),
            onClick = { viewModel.save() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        if (viewModel.isEditMode) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.exportPdf(context, signaturePath) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_export_pdf))
            }

            if (status == QuoteStatus.ACCEPTED) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.convertToInvoice() },
                    enabled = !isConverting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_convert_invoice))
                }
            }
        }
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

@Composable
fun AddItemDialog(
    onAdd: (String, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.quote_dialog_new_item)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MitoTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.quote_item_description),
                    modifier = Modifier.fillMaxWidth()
                )
                MitoNumberField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = stringResource(R.string.quote_item_quantity),
                    modifier = Modifier.fillMaxWidth()
                )
                MitoNumberField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = stringResource(R.string.quote_item_unit_price),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (description.isNotBlank()) {
                    onAdd(description, quantity.toDoubleOrNull() ?: 1.0, unitPrice.toDoubleOrNull() ?: 0.0)
                }
            }) {
                Text(stringResource(R.string.btn_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
