package com.example.lbo_marketplace.data.model

data class ProviderRequest(
    val userId: String = "",
    val email: String = "",
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)