package com.example.lbo_marketplace.auth

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()

    data class Authenticated(
        val uid: String,
        val role: String
    ) : AuthState()

    data class Error(val message: String) : AuthState()
}
