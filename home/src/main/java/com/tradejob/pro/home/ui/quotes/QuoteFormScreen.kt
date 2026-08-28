@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.home.ui.quotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tradejob.pro.components.MitoNumberField
import com.tradejob.pro.components.MitoTextField
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.components.SignaturePad
import com.tradejob.pro.core.navigation.Screen
import androidx.compose.ui.res.stringResource
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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(if (viewModel.isEditMode) stringResource(R.string.quote_form_edit) else stringResource(R.string.quote_form_new))
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
            QuoteFormContent(
                viewModel = viewModel,
                navController = navController,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun QuoteFormContent(
    viewModel: QuoteFormViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsState()
    val vatRate by viewModel.vatRate.collectAsState()
    val status by viewModel.status.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()
    val isConverting by viewModel.isConverting.collectAsState()
    val pdfFile by viewModel.pdfFile.collectAsState()
    val context = LocalContext.current

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
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            items.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.removeItem(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete_item))
                            }
                        }
                        Text(
                            text = "${item.quantity} x ${item.unitPrice} = ${item.total}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        IconButton(onClick = { showAddItem = true }) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.quote_btn_add_item))
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

        HorizontalDivider()

        // Totals
        val subtotal = viewModel.calculateSubtotal()
        val vatAmount = viewModel.calculateVatAmount()
        val total = viewModel.calculateTotal()

        Text(
            text = "${stringResource(R.string.quote_label_subtotal)}: ${"%.2f".format(subtotal)}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "${stringResource(R.string.quote_label_vat_amount)} (${vatRate.toInt()}%): ${"%.2f".format(vatAmount)}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "${stringResource(R.string.quote_label_total)}: ${"%.2f".format(total)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

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
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showSignatureDialog = false },
                title = { Text(stringResource(R.string.signature_dialog_title)) },
                text = {
                    SignaturePad(
                        context = androidx.compose.ui.platform.LocalContext.current,
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
                    androidx.compose.material3.TextButton(onClick = { showSignatureDialog = false }) {
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
            Text(if (signaturePath != null) stringResource(R.string.signature_btn_change) else stringResource(R.string.signature_btn_request))
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
                Spacer(modifier = Modifier.height(0.dp))
                Text("  ${stringResource(R.string.btn_export_pdf)}")
            }

            if (status == QuoteStatus.ACCEPTED) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.convertToInvoice() },
                    enabled = !isConverting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null)
                    Text("  ${stringResource(R.string.btn_convert_invoice)}")
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

    androidx.compose.material3.AlertDialog(
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
            androidx.compose.material3.TextButton(onClick = {
                if (description.isNotBlank()) {
                    onAdd(description, quantity.toDoubleOrNull() ?: 1.0, unitPrice.toDoubleOrNull() ?: 0.0)
                }
            }) {
                Text(stringResource(R.string.btn_add))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
