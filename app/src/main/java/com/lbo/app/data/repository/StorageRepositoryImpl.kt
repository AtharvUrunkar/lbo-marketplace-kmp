package com.lbo.app.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.lbo.app.domain.repository.StorageRepository
import com.lbo.app.utils.Constants
import com.lbo.app.utils.Resource
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): Resource<String> {
        return try {
            val ref = storage.reference
                .child("${Constants.STORAGE_PROFILE_IMAGES}/$userId/${UUID.randomUUID()}.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload profile image")
        }
    }

    override suspend fun uploadDocument(userId: String, documentUri: Uri): Resource<String> {
        return try {
            val ref = storage.reference
                .child("${Constants.STORAGE_DOCUMENTS}/$userId/${UUID.randomUUID()}")
            ref.putFile(documentUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload document")
        }
    }

    override suspend fun deleteFile(fileUrl: String): Resource<Boolean> {
        return try {
            storage.getReferenceFromUrl(fileUrl).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete file")
        }
    }
}
