package com.tradejob.pro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.tradejob.pro.components.theme.TradeJobTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tradejob.pro.navigation.AppNavigation
import com.tradejob.pro.login.ui.LoginScreen
import com.tradejob.pro.login.ui.NewUserScreen
import com.tradejob.pro.home.ui.HomeScreen
import com.tradejob.pro.home.ui.clients.ClientListScreen
import com.tradejob.pro.home.ui.clients.ClientFormScreen
import com.tradejob.pro.home.ui.clients.ClientDetailScreen
import com.tradejob.pro.home.ui.jobs.JobListScreen
import com.tradejob.pro.home.ui.jobs.JobFormScreen
import com.tradejob.pro.home.ui.profile.ProfileScreen
import com.tradejob.pro.home.ui.quotes.QuoteListScreen
import com.tradejob.pro.home.ui.quotes.QuoteFormScreen
import com.tradejob.pro.home.ui.invoices.InvoiceListScreen
import com.tradejob.pro.home.ui.invoices.InvoiceFormScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TradeJobTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val screens = listOf(
                        LoginScreen(),
                        NewUserScreen(),
                        HomeScreen(),
                        ClientListScreen(),
                        ClientFormScreen(),
                        ClientDetailScreen(),
                        JobListScreen(),
                        JobFormScreen(),
                        ProfileScreen(),
                        QuoteListScreen(),
                        QuoteFormScreen(),
                        InvoiceListScreen(),
                        InvoiceFormScreen()
                    )
                    AppNavigation(
                        navController = navController,
                        screens = screens,
                        startDestination = "login"
                    )
                }
            }
        }
    }
}
