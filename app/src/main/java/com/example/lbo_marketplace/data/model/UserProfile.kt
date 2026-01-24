package com.example.lbo_marketplace.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val role: String = "USER",   // USER | PROVIDER | ADMIN
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
