package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MovieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CarouselUiState {
    object Loading : CarouselUiState
    data class Success(val trendingMovies: List<MovieEntity>) : CarouselUiState
    data class Error(val message: String) : CarouselUiState
}

class MovieViewModel(private val movieRepository: MovieRepository) : ViewModel() {

    // Main Catalogs
    val allMovies: StateFlow<List<MovieEntity>> = movieRepository.allMovies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredMovies: StateFlow<List<MovieEntity>> = movieRepository.featuredMovies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingMovies: StateFlow<List<MovieEntity>> = movieRepository.trendingMovies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMovies: StateFlow<List<MovieEntity>> = movieRepository.latestMovies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = movieRepository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val carouselUiState: StateFlow<CarouselUiState> = movieRepository.trendingMovies
        .map { list ->
            if (list.isEmpty()) {
                CarouselUiState.Loading
            } else {
                CarouselUiState.Success(list)
            }
        }
        .catch { emit(CarouselUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarouselUiState.Loading)

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<MovieEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                movieRepository.searchMovies(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Movie Details State
    private val _selectedMovie = MutableStateFlow<MovieEntity?>(null)
    val selectedMovie = _selectedMovie.asStateFlow()

    private val _selectedMovieReviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val selectedMovieReviews = _selectedMovieReviews.asStateFlow()

    private val _isCurrentInWatchlist = MutableStateFlow(false)
    val isCurrentInWatchlist = _isCurrentInWatchlist.asStateFlow()

    // Watchlist and History for Active User
    private val _activeUserId = MutableStateFlow<String?>(null)

    val watchlist: StateFlow<List<MovieEntity>> = _activeUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else movieRepository.getWatchlist(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<MovieEntity>> = _activeUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else movieRepository.getWatchHistory(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawWatchHistory: StateFlow<List<WatchHistoryEntity>> = _activeUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else movieRepository.getWatchHistoryEntities(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulating Offline Downloads state mapping
    private val _downloadingStates = MutableStateFlow<Map<Int, Float>>(emptyMap()) // movieId -> progress (0.0 to 1.0)
    val downloadingStates = _downloadingStates.asStateFlow()

    private val _downloadedMovieIds = MutableStateFlow<Set<Int>>(emptySet())
    val downloadedMovieIds = _downloadedMovieIds.asStateFlow()

    init {
        viewModelScope.launch {
            // Auto populate with gorgeous movie data on startup if database is empty!
            movieRepository.prepopulateDatabaseIfEmpty()
        }
    }

    fun setActiveUser(userId: String?) {
        _activeUserId.value = userId
    }

    fun selectMovie(movieId: Int) {
        viewModelScope.launch {
            val movie = movieRepository.getMovieById(movieId)
            _selectedMovie.value = movie
            
            if (movie != null) {
                // Fetch reviews
                movieRepository.getReviews(movieId).collectLatest { reviews ->
                    _selectedMovieReviews.value = reviews
                }
            }
        }
        checkWatchlistState(movieId)
    }

    fun checkWatchlistState(movieId: Int) {
        val userId = _activeUserId.value ?: return
        viewModelScope.launch {
            _isCurrentInWatchlist.value = movieRepository.isMovieInWatchlist(userId, movieId)
        }
    }

    fun toggleWatchlist(movieId: Int) {
        val userId = _activeUserId.value ?: return
        viewModelScope.launch {
            val inWatchlist = movieRepository.isMovieInWatchlist(userId, movieId)
            if (inWatchlist) {
                movieRepository.removeFromWatchlist(userId, movieId)
                _isCurrentInWatchlist.value = false
            } else {
                movieRepository.addToWatchlist(userId, movieId)
                _isCurrentInWatchlist.value = true
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Submit user reviews
    fun submitReview(movieId: Int, username: String, rating: Int, comment: String) {
        val userId = _activeUserId.value ?: return
        viewModelScope.launch {
            movieRepository.addReview(userId, username, movieId, rating, comment)
            // Refresh reviews list
            movieRepository.getReviews(movieId).firstOrNull()?.let {
                _selectedMovieReviews.value = it
            }
        }
    }

    // Save Watch History progress
    fun saveWatchProgress(movieId: Int, progressMs: Long, durationMs: Long) {
        val userId = _activeUserId.value ?: return
        viewModelScope.launch {
            movieRepository.saveWatchHistory(userId, movieId, progressMs, durationMs)
        }
    }

    fun clearWatchHistory() {
        val userId = _activeUserId.value ?: return
        viewModelScope.launch {
            movieRepository.clearWatchHistory(userId)
        }
    }

    // Admin Upload / Content Management
    fun uploadMovie(
        title: String,
        description: String,
        rating: Double,
        year: Int,
        duration: String,
        genre: String,
        videoUrl: String,
        posterDrawableName: String = "img_poster_cyberpunk",
        isFeatured: Boolean = false,
        isTrending: Boolean = false,
        isLatest: Boolean = true,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val movie = MovieEntity(
                title = title,
                description = description,
                rating = rating,
                year = year,
                duration = duration,
                genre = genre,
                videoUrl = videoUrl,
                posterDrawableName = posterDrawableName,
                isFeatured = isFeatured,
                isTrending = isTrending,
                isLatest = isLatest
            )
            movieRepository.insertMovie(movie)
            onSuccess()
        }
    }

    // Download Simulation
    fun startMovieDownload(movieId: Int) {
        if (_downloadedMovieIds.value.contains(movieId) || _downloadingStates.value.containsKey(movieId)) return
        
        viewModelScope.launch {
            var progress = 0.0f
            while (progress <= 1.0f) {
                _downloadingStates.value = _downloadingStates.value.toMutableMap().apply {
                    put(movieId, progress)
                }
                kotlinx.coroutines.delay(400)
                progress += 0.15f
            }
            _downloadingStates.value = _downloadingStates.value.toMutableMap().apply {
                remove(movieId)
            }
            _downloadedMovieIds.value = _downloadedMovieIds.value.toMutableSet().apply {
                add(movieId)
            }
        }
    }

    fun removeDownloadedMovie(movieId: Int) {
        _downloadedMovieIds.value = _downloadedMovieIds.value.toMutableSet().apply {
            remove(movieId)
        }
    }
}
