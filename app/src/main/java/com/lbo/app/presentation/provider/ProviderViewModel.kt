package com.lbo.app.presentation.provider

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lbo.app.data.model.Booking
import com.lbo.app.data.model.Provider
import com.lbo.app.domain.repository.*
import com.lbo.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderProfileState(
    val isLoading: Boolean = false,
    val provider: Provider? = null,
    val isSaved: Boolean = false,
    val error: String? = null
)

data class ProviderBookingsState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val bookingRepository: BookingRepository,
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProviderProfileState())
    val profileState: StateFlow<ProviderProfileState> = _profileState.asStateFlow()

    private val _bookingsState = MutableStateFlow(ProviderBookingsState())
    val bookingsState: StateFlow<ProviderBookingsState> = _bookingsState.asStateFlow()

    init {
        loadProviderProfile()
    }

    fun loadProviderProfile() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            _profileState.value = _profileState.value.copy(isLoading = true)
            when (val result = providerRepository.getProviderByUserId(userId)) {
                is Resource.Success -> {
                    _profileState.value = ProviderProfileState(provider = result.data)
                    result.data?.let { loadBookings(it.providerId) }
                }
                is Resource.Error -> {
                    _profileState.value = ProviderProfileState(error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun saveProfile(
        name: String,
        category: String,
        location: String,
        description: String,
        experience: Int,
        profileImageUri: Uri?,
        documentUris: List<Uri>
    ) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, error = null)
            val userId = authRepository.currentUser?.uid ?: return@launch

            try {
                var profileImageUrl = _profileState.value.provider?.profileImage ?: ""
                val documentUrls = (_profileState.value.provider?.documentsUrl ?: emptyList()).toMutableList()

                // Upload profile image
                if (profileImageUri != null) {
                    when (val uploadResult = storageRepository.uploadProfileImage(userId, profileImageUri)) {
                        is Resource.Success -> profileImageUrl = uploadResult.data
                        is Resource.Error -> {
                            _profileState.value = _profileState.value.copy(
                                isLoading = false,
                                error = "Failed to upload profile image"
                            )
                            return@launch
                        }
                        is Resource.Loading -> {}
                    }
                }

                // Upload documents
                for (docUri in documentUris) {
                    when (val uploadResult = storageRepository.uploadDocument(userId, docUri)) {
                        is Resource.Success -> documentUrls.add(uploadResult.data)
                        is Resource.Error -> {}
                        is Resource.Loading -> {}
                    }
                }

                val existingProvider = _profileState.value.provider
                val provider = Provider(
                    providerId = existingProvider?.providerId ?: "",
                    userId = userId,
                    name = name,
                    category = category,
                    location = location,
                    description = description,
                    experience = experience,
                    rating = existingProvider?.rating ?: 0.0,
                    totalReviews = existingProvider?.totalReviews ?: 0,
                    isApproved = existingProvider?.isApproved ?: false,
                    profileImage = profileImageUrl,
                    documentsUrl = documentUrls
                )

                val result = if (existingProvider != null) {
                    providerRepository.updateProvider(provider)
                } else {
                    providerRepository.createProvider(provider)
                }

                when (result) {
                    is Resource.Success -> {
                        // Also update user profile image
                        userRepository.getUser(userId).let { userResult ->
                            if (userResult is Resource.Success) {
                                userRepository.updateUser(
                                    userResult.data.copy(
                                        profileImage = profileImageUrl,
                                        name = name,
                                        location = location,
                                        isVerified = false
                                    )
                                )
                            }
                        }
                        _profileState.value = ProviderProfileState(
                            provider = provider,
                            isSaved = true
                        )
                        loadProviderProfile()
                    }
                    is Resource.Error -> {
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadBookings(providerId: String) {
        viewModelScope.launch {
            _bookingsState.value = _bookingsState.value.copy(isLoading = true)
            when (val result = bookingRepository.getBookingsByProvider(providerId)) {
                is Resource.Success -> {
                    _bookingsState.value = ProviderBookingsState(
                        isLoading = false,
                        bookings = result.data
                    )
                }
                is Resource.Error -> {
                    _bookingsState.value = ProviderBookingsState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun refreshBookings() {
        _profileState.value.provider?.let { loadBookings(it.providerId) }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, status)
            refreshBookings()
        }
    }
}
