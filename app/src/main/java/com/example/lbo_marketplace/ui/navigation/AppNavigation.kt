package com.example.lbo_marketplace.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.auth.*
import com.example.lbo_marketplace.auth.AuthSessionTestScreen
import com.example.lbo_marketplace.ui.screens.*

@Composable
fun AppNavigation(viewModel: AuthViewModel = viewModel()) {

    val state = viewModel.authState.value

    // 🔥 AUTO SESSION CHECK
    LaunchedEffect(Unit) {
        viewModel.checkSession()
    }

    when (state) {

        is AuthState.Loading -> {
            CircularProgressIndicator()
        }

        is AuthState.Unauthenticated -> {
            AuthSessionTestScreen(viewModel)
        }

        is AuthState.Authenticated -> {

            when (state.role) {

                "USER" -> UserHomeScreen()

                "SERVICE_PROVIDER" -> ProviderDashboard()

                "ADMIN" -> AdminPanel()

                else -> AuthSessionTestScreen(viewModel)
            }
        }

        is AuthState.Error -> {
            Text("Error: ${state.message}")
        }

        else -> {}
    }
}