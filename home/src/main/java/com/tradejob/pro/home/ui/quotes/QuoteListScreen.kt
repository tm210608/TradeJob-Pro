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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.core.navigation.Screen
import com.tradejob.pro.database.data.entity.QuoteEntity
import com.tradejob.pro.home.domain.GetQuotesByClientUseCase
import androidx.compose.ui.res.stringResource
import com.tradejob.pro.home.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuoteListScreen : Screen {
    override val route: String = "quote_list/{clientId}"

    override val arguments = listOf(
        navArgument("clientId") {
            type = NavType.StringType
        }
    )

    @Composable
    override fun Content(navController: NavHostController) {
        val viewModel: QuoteListViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.quote_list_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("quote_form/${viewModel.clientId}/0") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.quote_form_new))
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            QuoteListContent(
                viewModel = viewModel,
                navController = navController,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@HiltViewModel
class QuoteListViewModel @Inject constructor(
    private val getQuotesByClientUseCase: GetQuotesByClientUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val clientId: Long = savedStateHandle.get<String>("clientId")?.toLongOrNull() ?: 0L

    private val _quotes = MutableStateFlow<List<QuoteEntity>>(emptyList())
    val quotes: StateFlow<List<QuoteEntity>> = _quotes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableStateFlow<QuoteListEvent>(QuoteListEvent.None)
    val event: StateFlow<QuoteListEvent> = _event

    init {
        loadQuotes(clientId)
    }

    private fun loadQuotes(id: Long) {
        viewModelScope.launch {
            getQuotesByClientUseCase(id).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _quotes.value = result.data
                        _isLoading.value = false
                    }
                    is Result.Error -> {
                        _event.value = QuoteListEvent.Error("Error al cargar presupuestos")
                        _isLoading.value = false
                    }
                    is Result.Loading -> _isLoading.value = true
                }
            }
        }
    }

    fun clearEvent() {
        _event.value = QuoteListEvent.None
    }
}

sealed class QuoteListEvent {
    data object None : QuoteListEvent()
    data class Error(val message: String) : QuoteListEvent()
}

@Composable
fun QuoteListContent(
    viewModel: QuoteListViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val quotes by viewModel.quotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()

    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is QuoteListEvent.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            quotes.isEmpty() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.quote_empty_list),
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
                    items(quotes, key = { it.id }) { quote ->
                        QuoteCard(
                            quote = quote,
                            onClick = { navController.navigate("quote_form/${viewModel.clientId}/${quote.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteCard(
    quote: QuoteEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = quote.number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val status = QuoteStatus.fromValue(quote.status)
                Text(
                    text = stringResource(status.displayRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (status) {
                        QuoteStatus.ACCEPTED -> MaterialTheme.colorScheme.primary
                        QuoteStatus.REJECTED -> MaterialTheme.colorScheme.error
                        QuoteStatus.SENT -> MaterialTheme.colorScheme.tertiary
                        QuoteStatus.DRAFT -> MaterialTheme.colorScheme.outline
                    },
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total: ${"%.2f".format(quote.total)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
