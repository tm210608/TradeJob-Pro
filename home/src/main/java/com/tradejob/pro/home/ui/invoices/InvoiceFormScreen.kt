@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.home.ui.invoices

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tradejob.pro.components.MitoTextField
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.home.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceFormScreen : Screen {
    override val route: String = "invoice_form/{invoiceId}"

    override val arguments = listOf(
        navArgument("invoiceId") {
            type = NavType.StringType
        }
    )

    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: InvoiceFormViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.invoice_form_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            InvoiceFormContent(
                viewModel = viewModel,
                navController = navController,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun InvoiceFormContent(
    viewModel: InvoiceFormViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val invoice by viewModel.invoice.collectAsState()
    val client by viewModel.client.collectAsState()
    val status by viewModel.status.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is InvoiceFormEvent.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            is InvoiceFormEvent.Success -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                if (currentEvent.message.contains("eliminada", ignoreCase = true)) {
                    navController.popBackStack()
                }
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

    if (isLoading && invoice == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentInvoice = invoice ?: return
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = currentInvoice.number,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        client?.let { c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(c.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text("Tel: ${c.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    c.address?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline) }
                }
            }
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.invoice_label_status),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InvoiceStatus.entries.forEach { s ->
                FilterChip(
                    selected = status == s,
                    onClick = { viewModel.onStatusChanged(s) },
                    label = { Text(stringResource(s.displayRes)) }
                )
            }
        }

        var paymentExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = paymentExpanded,
            onExpandedChange = { paymentExpanded = it }
        ) {
            TextField(
                value = paymentMethod ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.invoice_label_payment_method)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = paymentExpanded,
                onDismissRequest = { paymentExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.payment_cash)) },
                    onClick = { viewModel.onPaymentMethodChanged("CASH"); paymentExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.payment_card)) },
                    onClick = { viewModel.onPaymentMethodChanged("CARD"); paymentExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.payment_transfer)) },
                    onClick = { viewModel.onPaymentMethodChanged("TRANSFER"); paymentExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.payment_bizum)) },
                    onClick = { viewModel.onPaymentMethodChanged("BIZUM"); paymentExpanded = false }
                )
            }
        }

        HorizontalDivider()

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${stringResource(R.string.invoice_label_issued)}: ${dateFormat.format(Date(currentInvoice.issuedAt))}")
                currentInvoice.dueAt?.let {
                    Text("${stringResource(R.string.invoice_label_due)}: ${dateFormat.format(Date(it))}")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${stringResource(R.string.invoice_label_subtotal)}: ${"%.2f".format(currentInvoice.subtotal)}")
                Text("${stringResource(R.string.invoice_label_vat)}: ${"%.2f".format(currentInvoice.vatAmount)}")
                currentInvoice.irpfRate?.let { irpf ->
                    val irpfAmount = currentInvoice.subtotal * irpf / 100.0
                    Text("${stringResource(R.string.invoice_label_irpf)} (-${irpf.toInt()}%): -${"%.2f".format(irpfAmount)}")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.invoice_label_total)}: ${"%.2f".format(currentInvoice.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (currentInvoice.paidAmount > 0) {
                    Text(
                        text = "${stringResource(R.string.invoice_label_paid)}: ${"%.2f".format(currentInvoice.paidAmount)} / ${stringResource(R.string.invoice_label_pending)}: ${"%.2f".format(currentInvoice.total - currentInvoice.paidAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        MitoTextField(
            value = notes,
            onValueChange = { viewModel.onNotesChanged(it) },
            label = stringResource(R.string.invoice_label_notes),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (status != InvoiceStatus.PAID) {
            OutlinedButton(
                onClick = { viewModel.markAsPaid() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.invoice_btn_mark_paid))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = { viewModel.exportPdf(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
            Text("  ${stringResource(R.string.btn_export_pdf)}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = stringResource(R.string.invoice_btn_save),
            onClick = { viewModel.save() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Text("  ${stringResource(R.string.invoice_btn_delete)}")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.invoice_delete_title)) },
            text = { Text(stringResource(R.string.invoice_delete_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) {
                    Text(stringResource(R.string.btn_delete_item))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
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
