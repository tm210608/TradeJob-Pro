package com.tradejob.pro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tradejob.pro.core.navigation.Screen

@Composable
fun AppNavigation(
    navController: NavHostController,
    screens: List<Screen>,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        screens.forEach { screen ->
            composable(
                route = screen.route,
                arguments = screen.arguments
            ) { 
                screen.Content(navController) 
            }
        }
    }
}
