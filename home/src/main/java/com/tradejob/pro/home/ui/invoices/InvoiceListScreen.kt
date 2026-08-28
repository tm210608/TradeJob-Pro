@file:OptIn(ExperimentalMaterial3Api::class)

package com.tradejob.pro.home.ui.invoices

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.InvoiceEntity
import com.tradejob.pro.database.data.entity.UserEntity
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.GenerateInvoicePdfUseCase
import com.tradejob.pro.home.domain.GetAllInvoicesUseCase
import com.tradejob.pro.home.domain.UserRepository
import androidx.compose.ui.res.stringResource
import com.tradejob.pro.home.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class InvoiceStatus(val value: String, val displayRes: Int) {
    PENDING("PENDING", R.string.invoice_status_pending),
    PAID("PAID", R.string.invoice_status_paid),
    OVERDUE("OVERDUE", R.string.invoice_status_overdue),
    CANCELLED("CANCELLED", R.string.invoice_status_cancelled);

    companion object {
        fun fromValue(value: String): InvoiceStatus = entries.find { it.value == value } ?: PENDING
    }
}

class InvoiceListScreen : Screen {
    override val route: String = "invoice_list"

    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: InvoiceListViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.invoice_list_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            InvoiceListContent(
                viewModel = viewModel,
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val getAllInvoicesUseCase: GetAllInvoicesUseCase,
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository,
    private val generateInvoicePdfUseCase: GenerateInvoicePdfUseCase
) : ViewModel() {

    private val _allInvoices = MutableStateFlow<List<InvoiceEntity>>(emptyList())
    private val _selectedFilter = MutableStateFlow<InvoiceStatus?>(null)
    val selectedFilter: StateFlow<InvoiceStatus?> = _selectedFilter

    val invoices: StateFlow<List<InvoiceEntity>> = combine(_allInvoices, _selectedFilter) { all, filter ->
        if (filter == null) all else all.filter { it.status == filter.value }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadInvoices()
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

    fun onFilterSelected(status: InvoiceStatus?) {
        _selectedFilter.value = status
    }

    private val _clients = MutableStateFlow<Map<Long, ClientEntity>>(emptyMap())
    private var professional: UserEntity? = null

    private val _pdfMessage = MutableStateFlow<String?>(null)
    val pdfMessage: StateFlow<String?> = _pdfMessage

    init {
        loadProfessional()
    }

    private fun loadProfessional() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { user ->
                professional = user
            }
        }
    }

    fun exportInvoicePdf(context: Context, invoice: InvoiceEntity) {
        viewModelScope.launch {
            val client = _clients.value[invoice.clientId] ?: run {
                when (val r = clientRepository.getClientById(invoice.clientId)) {
                    is Result.Success -> {
                        r.data?.let { c ->
                            _clients.value = _clients.value + (c.id to c)
                            c
                        }
                    }
                    else -> null
                }
            }
            if (client == null) {
                _pdfMessage.value = "Error: cliente no encontrado"
                return@launch
            }
            val file = generateInvoicePdfUseCase(context, client, invoice, professional)
            _pdfMessage.value = if (file != null) "PDF generado: ${file.name}" else "Error al generar PDF"
        }
    }

    fun clearPdfMessage() {
        _pdfMessage.value = null
    }
}

@Composable
fun InvoiceListContent(
    viewModel: InvoiceListViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val invoices by viewModel.invoices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val pdfMessage by viewModel.pdfMessage.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pdfMessage) {
        pdfMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearPdfMessage()
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
                isLoading -> CircularProgressIndicator()
                invoices.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.height(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.invoice_empty_list),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                    ) {
                        items(invoices, key = { it.id }) { invoice ->
                            InvoiceCard(
                                invoice = invoice,
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
fun InvoiceCard(invoice: InvoiceEntity, onExportPdf: () -> Unit = {}, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val status = InvoiceStatus.fromValue(invoice.status)
                Text(
                    text = stringResource(status.displayRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (status) {
                        InvoiceStatus.PAID -> MaterialTheme.colorScheme.primary
                        InvoiceStatus.OVERDUE -> MaterialTheme.colorScheme.error
                        InvoiceStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                        InvoiceStatus.CANCELLED -> MaterialTheme.colorScheme.outline
                    },
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total: ${"%.2f".format(invoice.total)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            if (invoice.paidAmount > 0 && invoice.paidAmount < invoice.total) {
                Text(
                    text = "Pagado: ${"%.2f".format(invoice.paidAmount)} / Pendiente: ${"%.2f".format(invoice.total - invoice.paidAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onExportPdf,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Text("  ${stringResource(R.string.btn_export_pdf)}")
            }
        }
    }
}
