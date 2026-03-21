package com.example.lbo_marketplace.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authManager = FirebaseAuthManager()

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authManager.register(name, email, password)

            _authState.value = result.fold(
                onSuccess = { AuthState.Authenticated(it.first, it.second) },
                onFailure = { AuthState.Error(it.message ?: "Error") }
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authManager.login(email, password)

            _authState.value = result.fold(
                onSuccess = { AuthState.Authenticated(it.first, it.second) },
                onFailure = { AuthState.Error(it.message ?: "Error") }
            )
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authManager.checkSession()

            _authState.value = result.fold(
                onSuccess = { AuthState.Authenticated(it.first, it.second) },
                onFailure = { AuthState.Unauthenticated }
            )
        }
    }

    fun logout() {
        authManager.logout()
        _authState.value = AuthState.Unauthenticated
    }
}