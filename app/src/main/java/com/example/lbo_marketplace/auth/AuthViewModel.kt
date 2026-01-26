package com.example.lbo_marketplace.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authManager: FirebaseAuthManager = FirebaseAuthManager()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            authManager.login(email, password)
                .onSuccess { (uid, role) ->
                    _authState.value = AuthState.Authenticated(uid, role)
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Login failed")
                }
        }
    }

    fun checkSession() {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            authManager.checkSession()
                .onSuccess { (uid, role) ->
                    _authState.value = AuthState.Authenticated(uid, role)
                }
                .onFailure {
                    _authState.value = AuthState.Idle
                }
        }
    }
}
