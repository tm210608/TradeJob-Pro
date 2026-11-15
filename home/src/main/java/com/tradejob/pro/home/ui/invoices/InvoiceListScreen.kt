@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.home.ui.invoices

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.tradejob.pro.common.analytics.AnalyticsHelper
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.components.theme.BoneCard
import com.tradejob.pro.components.theme.BoneLight
import com.tradejob.pro.components.theme.Petrol20
import com.tradejob.pro.components.theme.Petrol30
import com.tradejob.pro.components.theme.SystemBarIconStyle
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.InvoiceEntity
import com.tradejob.pro.database.data.entity.UserEntity
import com.tradejob.pro.home.R
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.GenerateInvoicePdfUseCase
import com.tradejob.pro.home.domain.GetAllInvoicesUseCase
import com.tradejob.pro.home.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class InvoiceListScreen : Screen {
    override val route: String = "invoice_list"

    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: InvoiceListViewModel = hiltViewModel()
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
                            text = stringResource(R.string.invoice_list_title),
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
                InvoiceListContent(
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

@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val getAllInvoicesUseCase: GetAllInvoicesUseCase,
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository,
    private val generateInvoicePdfUseCase: GenerateInvoicePdfUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _allInvoices = MutableStateFlow<List<InvoiceEntity>>(emptyList())
    private val _selectedFilter = MutableStateFlow<InvoiceStatus?>(null)
    val selectedFilter: StateFlow<InvoiceStatus?> = _selectedFilter

    val invoices: StateFlow<List<InvoiceEntity>> = combine(_allInvoices, _selectedFilter) { all, filter ->
        if (filter == null) all else all.filter { it.status == filter.value }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _clientNames = MutableStateFlow<Map<Long, String>>(emptyMap())
    val clientNames: StateFlow<Map<Long, String>> = _clientNames

    private var clientsById: Map<Long, ClientEntity> = emptyMap()
    private var professional: UserEntity? = null

    private val _pdfEvent = MutableStateFlow<InvoicePdfEvent?>(null)
    val pdfEvent: StateFlow<InvoicePdfEvent?> = _pdfEvent

    init {
        loadInvoices()
        loadClients()
        loadProfessional()
    }

    private fun loadInvoices() {
        viewModelScope.launch {
            getAllInvoicesUseCase().collect { result ->
                when (result) {
                    is Result.Success -> { _allInvoices.value = result.data; _isLoading.value = false }
                    is Result.Loading -> _isLoading.value = true
                    else -> _isLoading.value = false
                }
            }
        }
    }

    private fun loadClients() {
        viewModelScope.launch {
            clientRepository.getAllClients().collect { result ->
                if (result is Result.Success) {
                    clientsById = result.data.associateBy { it.id }
                    _clientNames.value = clientsById.mapValues { it.value.name }
                }
            }
        }
    }

    private fun loadProfessional() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { user ->
                professional = user
            }
        }
    }

    fun onFilterSelected(status: InvoiceStatus?) {
        _selectedFilter.value = status
    }

    fun exportInvoicePdf(context: Context, invoice: InvoiceEntity) {
        viewModelScope.launch {
            val client = clientsById[invoice.clientId]
            if (client == null) {
                _pdfEvent.value = InvoicePdfEvent(R.string.invoice_error_client)
                return@launch
            }
            val file = generateInvoicePdfUseCase(context, client, invoice, professional)
            _pdfEvent.value = if (file != null) {
                analyticsHelper.logEvent("invoice_pdf_exported", mapOf("invoiceId" to invoice.id.toString()))
                InvoicePdfEvent(R.string.invoice_pdf_success_named, file.name)
            } else {
                InvoicePdfEvent(R.string.invoice_pdf_error)
            }
        }
    }

    fun clearPdfEvent() {
        _pdfEvent.value = null
    }
}

data class InvoicePdfEvent(@StringRes val messageRes: Int, val arg: String? = null)

@Composable
fun InvoiceListContent(
    viewModel: InvoiceListViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val invoices by viewModel.invoices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val clientNames by viewModel.clientNames.collectAsState()
    val pdfEvent by viewModel.pdfEvent.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(pdfEvent) {
        pdfEvent?.let {
            snackbarHostState.showSnackbar(
                it.arg?.let { arg -> context.getString(it.messageRes, arg) }
                    ?: context.getString(it.messageRes)
            )
            viewModel.clearPdfEvent()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.onFilterSelected(null) },
                    label = { Text(stringResource(R.string.invoice_filter_all)) }
                )
            }
            InvoiceStatus.entries.forEach { status ->
                item {
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { viewModel.onFilterSelected(status) },
                        label = { Text(stringResource(status.displayRes)) }
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                invoices.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.height(64.dp),
                            tint = if (darkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.invoice_empty_list),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (darkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(invoices, key = { it.id }) { invoice ->
                            InvoiceCard(
                                invoice = invoice,
                                clientName = clientNames[invoice.clientId],
                                onExportPdf = { viewModel.exportInvoicePdf(context, invoice) },
                                onClick = { navController.navigate("invoice_form/${invoice.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(
    invoice: InvoiceEntity,
    clientName: String?,
    onExportPdf: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val darkTheme = isSystemInDarkTheme()
    val status = InvoiceStatus.fromValue(invoice.status)
    val contentColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val subtleColor = if (darkTheme) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant

    val date = formatInvoiceDate(invoice.issuedAt)
    val amount = formatInvoiceAmount(invoice.total)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Text(
                    text = stringResource(status.displayRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = status.displayColor,
                    fontWeight = FontWeight.Bold
                )
            }
            clientName?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = listOfNotNull(date, amount).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = subtleColor
            )
            if (invoice.paidAmount > 0 && invoice.paidAmount < invoice.total) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.invoice_paid_partial,
                        formatInvoiceAmount(invoice.paidAmount),
                        formatInvoiceAmount(invoice.total - invoice.paidAmount)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = subtleColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onExportPdf,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_export_pdf))
            }
        }
    }
}
