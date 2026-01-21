package com.example.lbo_marketplace.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Register user with email & password
     */
    suspend fun signUp(
        email: String,
        password: String
    ): FirebaseUser? {
        val result = auth
            .createUserWithEmailAndPassword(email, password)
            .await()

        return result.user
    }

    /**
     * Login user with email & password
     */
    suspend fun login(
        email: String,
        password: String
    ): FirebaseUser? {
        val result = auth
            .signInWithEmailAndPassword(email, password)
            .await()

        return result.user
    }

    /**
     * Logout current user
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Get currently logged-in user
     */
    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Get Firebase ID Token (JWT)
     * This will be sent to backend / Firestore
     */
    suspend fun getIdToken(): String? {
        val user = auth.currentUser ?: return null
        val tokenResult = user.getIdToken(true).await()
        return tokenResult.token
    }
}
