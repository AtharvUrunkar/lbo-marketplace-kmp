package com.lbo.app.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lbo.app.data.model.*
import com.lbo.app.domain.repository.*
import com.lbo.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val topRatedProviders: List<Provider> = emptyList(),
    val nearbyProviders: List<Provider> = emptyList(),
    val communityPosts: List<CommunityPost> = emptyList(),
    val error: String? = null
)

data class SearchState(
    val isLoading: Boolean = false,
    val query: String = "",
    val results: List<Provider> = emptyList(),
    val selectedCategory: String? = null,
    val error: String? = null
)

data class BookingFormState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val selectedDate: String = "",
    val selectedTime: String = ""
)

data class MyBookingsState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val bookingRepository: BookingRepository,
    private val categoryRepository: CategoryRepository,
    private val communityRepository: CommunityRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _bookingFormState = MutableStateFlow(BookingFormState())
    val bookingFormState: StateFlow<BookingFormState> = _bookingFormState.asStateFlow()

    private val _myBookingsState = MutableStateFlow(MyBookingsState())
    val myBookingsState: StateFlow<MyBookingsState> = _myBookingsState.asStateFlow()

    private val _selectedProvider = MutableStateFlow<Provider?>(null)
    val selectedProvider: StateFlow<Provider?> = _selectedProvider.asStateFlow()

    private val _providerReviews = MutableStateFlow<List<Review>>(emptyList())
    val providerReviews: StateFlow<List<Review>> = _providerReviews.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true)
            try {
                val categoriesResult = categoryRepository.getCategories()
                val topRatedResult = providerRepository.getTopRatedProviders()
                val providersResult = providerRepository.getApprovedProviders()
                val postsResult = communityRepository.getPosts()

                _homeState.value = HomeState(
                    isLoading = false,
                    categories = (categoriesResult as? Resource.Success)?.data ?: emptyList(),
                    topRatedProviders = (topRatedResult as? Resource.Success)?.data ?: emptyList(),
                    nearbyProviders = (providersResult as? Resource.Success)?.data ?: emptyList(),
                    communityPosts = (postsResult as? Resource.Success)?.data?.take(3) ?: emptyList()
                )
            } catch (e: Exception) {
                _homeState.value = HomeState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun searchProviders(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            if (query.isNotBlank()) {
                _searchState.value = _searchState.value.copy(isLoading = true)
                when (val result = providerRepository.searchProviders(query)) {
                    is Resource.Success -> {
                        _searchState.value = _searchState.value.copy(
                            isLoading = false,
                            results = result.data
                        )
                    }
                    is Resource.Error -> {
                        _searchState.value = _searchState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {}
                }
            } else {
                _searchState.value = SearchState()
            }
        }
    }

    fun filterByCategory(category: String) {
        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(
                isLoading = true,
                selectedCategory = category
            )
            when (val result = providerRepository.getProvidersByCategory(category)) {
                is Resource.Success -> {
                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        results = result.data
                    )
                }
                is Resource.Error -> {
                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadProviderDetails(providerId: String) {
        viewModelScope.launch {
            when (val result = providerRepository.getProvider(providerId)) {
                is Resource.Success -> _selectedProvider.value = result.data
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
            when (val result = reviewRepository.getReviewsByProvider(providerId)) {
                is Resource.Success -> _providerReviews.value = result.data
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun createBooking(providerId: String, providerName: String, category: String, date: String, time: String) {
        viewModelScope.launch {
            _bookingFormState.value = _bookingFormState.value.copy(isLoading = true)
            val userId = authRepository.currentUser?.uid ?: return@launch
            val userResult = userRepository.getUser(userId)
            val userName = (userResult as? Resource.Success)?.data?.name ?: "Customer"

            val booking = Booking(
                customerId = userId,
                customerName = userName,
                providerId = providerId,
                providerName = providerName,
                category = category,
                date = date,
                time = time
            )
            when (val result = bookingRepository.createBooking(booking)) {
                is Resource.Success -> {
                    _bookingFormState.value = BookingFormState(isSuccess = true)
                }
                is Resource.Error -> {
                    _bookingFormState.value = _bookingFormState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadMyBookings() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            _myBookingsState.value = _myBookingsState.value.copy(isLoading = true)
            when (val result = bookingRepository.getBookingsByCustomer(userId)) {
                is Resource.Success -> {
                    _myBookingsState.value = MyBookingsState(
                        isLoading = false,
                        bookings = result.data
                    )
                }
                is Resource.Error -> {
                    _myBookingsState.value = MyBookingsState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun submitReview(bookingId: String, providerId: String, rating: Float, comment: String) {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            val userResult = userRepository.getUser(userId)
            val userName = (userResult as? Resource.Success)?.data?.name ?: "Customer"

            val review = Review(
                bookingId = bookingId,
                customerId = userId,
                customerName = userName,
                providerId = providerId,
                rating = rating,
                comment = comment
            )
            reviewRepository.addReview(review)

            // Update provider rating
            val reviewsResult = reviewRepository.getReviewsByProvider(providerId)
            if (reviewsResult is Resource.Success) {
                val reviews = reviewsResult.data
                val avgRating = reviews.map { it.rating.toDouble() }.average()
                providerRepository.updateRating(providerId, avgRating, reviews.size)
            }
        }
    }

    fun resetBookingForm() {
        _bookingFormState.value = BookingFormState()
    }
}
