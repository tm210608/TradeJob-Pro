package com.tradejob.pro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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
                        ProfileScreen()
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
