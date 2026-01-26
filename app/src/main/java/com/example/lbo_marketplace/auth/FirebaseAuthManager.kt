package com.example.lbo_marketplace.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Result<Pair<String, String>> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID null"))

            val snapshot = db.collection("users").document(uid).get().await()
            val role = snapshot.getString("role") ?: "USER"

            Result.success(uid to role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkSession(): Result<Pair<String, String>> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))

        return try {
            val snapshot = db.collection("users").document(user.uid).get().await()
            val role = snapshot.getString("role") ?: "USER"
            Result.success(user.uid to role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
