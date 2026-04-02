package com.example.lbo_marketplace.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lbo_marketplace.data.repository.ProviderRepository
import kotlinx.coroutines.launch

class ProviderViewModel : ViewModel() {

    private val repo = ProviderRepository()

    var applyState by mutableStateOf("")
        private set

    fun apply(userId: String, email: String) {

        viewModelScope.launch {
            applyState = "Loading..."

            val result = repo.applyForProvider(userId, email)

            applyState = result.fold(
                onSuccess = { it },
                onFailure = { it.message ?: "Error" }
            )
        }
    }
}