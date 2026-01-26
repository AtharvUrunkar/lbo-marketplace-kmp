package com.example.lbo_marketplace.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthSessionTestScreen() {

    val viewModel: AuthViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.checkSession()
    }

    val state = viewModel.authState.collectAsState().value

    Text(text = state.toString())
}
