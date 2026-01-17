package com.example.lbo_marketplace

import androidx.lifecycle.ViewModel
import com.example.shared.Greeting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GreetingViewModel : ViewModel() {

    private val greeting = Greeting()

    private val _uiState = MutableStateFlow("")
    val uiState: StateFlow<String> = _uiState

    init {
        loadGreeting()
    }

    private fun loadGreeting() {
        _uiState.value = greeting.greet()
    }
}
