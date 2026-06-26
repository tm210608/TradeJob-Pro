package com.tradejob.pro.login.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun loginButton_isDisabled_whenEmailAndPasswordAreEmpty() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            LoginContent(
                status = Status(),
                event = Event.None,
                onLoginClick = {},
                onLoginChanged = { _, _ -> },
                onRegisterClick = {},
                onEventHandled = {},
                navController = navController,
                snackbarHostState = androidx.compose.material3.SnackbarHostState(),
            )
        }

        composeTestRule.onNodeWithText("ENTRAR").assertIsNotEnabled()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun loginButton_isEnabled_whenEmailAndPasswordAreNotEmpty() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            LoginContent(
                status = Status(email = "test@example.com", password = "password", loginEnable = true),
                event = Event.None,
                onLoginClick = {},
                onLoginChanged = { _, _ -> },
                onRegisterClick = {},
                onEventHandled = {},
                navController = navController,
                snackbarHostState = androidx.compose.material3.SnackbarHostState(),
            )
        }

        composeTestRule.onNodeWithText("ENTRAR").assertIsEnabled()
    }
}
