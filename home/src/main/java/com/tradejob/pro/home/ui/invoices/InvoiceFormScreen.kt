@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tradejob.pro.home.ui.invoices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.tradejob.pro.components.MitoTextField
import com.tradejob.pro.components.PrimaryButton
import com.tradejob.pro.components.theme.BoneCard
import com.tradejob.pro.components.theme.BoneLight
import com.tradejob.pro.components.theme.Petrol20
import com.tradejob.pro.components.theme.Petrol30
import com.tradejob.pro.components.theme.SystemBarIconStyle
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.home.R

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
        val topBarContentColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = viewModel.invoice.collectAsState().value?.number
                                ?: stringResource(R.string.invoice_form_title),
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
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().then(backgroundModifier)) {
                InvoiceFormContent(
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
fun InvoiceFormContent(
    viewModel: InvoiceFormViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    darkTheme: Boolean,
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
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        MitoBottomSheet(
            mitoButtonSheet = MitoButtonSheet.ConfirmMitoButtonSheet(
                title = stringResource(R.string.invoice_delete_title),
                message = stringResource(R.string.invoice_delete_msg),
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

    val contentColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val subtleColor = if (darkTheme) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is InvoiceFormEvent.Error -> {
                snackbarHostState.showSnackbar(context.getString(currentEvent.messageRes))
                viewModel.clearEvent()
            }
            is InvoiceFormEvent.Success -> {
                snackbarHostState.showSnackbar(context.getString(currentEvent.messageRes))
                if (currentEvent.messageRes == R.string.invoice_success_deleted) {
                    navController.popBackStack()
                }
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

    if (isLoading && invoice == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val currentInvoice = invoice ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        client?.let { c ->
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
                    Text(
                        c.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                    Text(
                        "Tel: ${c.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtleColor
                    )
                    c.address?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = subtleColor)
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.invoice_label_status),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        FlowRow(
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
                value = paymentMethod?.let { stringResource(paymentMethodRes(it)) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.invoice_label_payment_method)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            DropdownMenu(
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
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${stringResource(R.string.invoice_label_issued)}: ${formatInvoiceDate(currentInvoice.issuedAt).orEmpty()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
                currentInvoice.dueAt?.let {
                    Text(
                        "${stringResource(R.string.invoice_label_due)}: ${formatInvoiceDate(it).orEmpty()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${stringResource(R.string.invoice_label_subtotal)}: ${formatInvoiceAmount(currentInvoice.subtotal)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
                Text(
                    "${stringResource(R.string.invoice_label_vat)}: ${formatInvoiceAmount(currentInvoice.vatAmount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
                currentInvoice.irpfRate?.let { irpf ->
                    val irpfAmount = currentInvoice.subtotal * irpf / 100.0
                    Text(
                        "${stringResource(R.string.invoice_label_irpf)} (-${irpf.toInt()}%): -${formatInvoiceAmount(irpfAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.invoice_label_total)}: ${formatInvoiceAmount(currentInvoice.total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (currentInvoice.paidAmount > 0) {
                    Text(
                        text = stringResource(
                            R.string.invoice_paid_partial,
                            formatInvoiceAmount(currentInvoice.paidAmount),
                            formatInvoiceAmount(currentInvoice.total - currentInvoice.paidAmount)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = subtleColor
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
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_export_pdf))
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
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.invoice_btn_delete))
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

private fun paymentMethodRes(value: String): Int = when (value) {
    "CASH" -> R.string.payment_cash
    "CARD" -> R.string.payment_card
    "TRANSFER" -> R.string.payment_transfer
    "BIZUM" -> R.string.payment_bizum
    else -> R.string.payment_cash
}
