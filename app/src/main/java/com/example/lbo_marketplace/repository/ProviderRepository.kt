package com.example.lbo_marketplace.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProviderRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun applyForProvider(
        userId: String,
        email: String
    ): Result<String> {

        return try {

            // 🔍 STEP 1: Check if user already a provider
            val userDoc = db.collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                return Result.failure(Exception("User not found"))
            }

            val role = userDoc.getString("role")

            if (role == "SERVICE_PROVIDER") {
                return Result.failure(Exception("Already a service provider"))
            }

            // 🔍 STEP 2: Check existing request
            val existingRequest = db.collection("provider_requests")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!existingRequest.isEmpty) {
                return Result.failure(Exception("Request already pending"))
            }

            // ✅ STEP 3: Create new request
            val request = hashMapOf(
                "userId" to userId,
                "email" to email,
                "status" to "PENDING",
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("provider_requests")
                .add(request)
                .await()

            Result.success("Application submitted successfully")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}