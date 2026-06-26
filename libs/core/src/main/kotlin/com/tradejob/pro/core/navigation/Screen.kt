package com.tradejob.pro.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController

interface Screen {
    val route: String
    val arguments: List<NamedNavArgument> get() = emptyList()

    @Composable
    fun Content(navController: NavHostController)
}
