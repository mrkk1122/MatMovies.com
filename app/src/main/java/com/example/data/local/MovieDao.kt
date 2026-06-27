package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    // Movies
    @Query("SELECT * FROM movies ORDER BY id DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isFeatured = 1 ORDER BY id DESC")
    fun getFeaturedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isTrending = 1 ORDER BY id DESC")
    fun getTrendingMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isLatest = 1 ORDER BY id DESC")
    fun getLatestMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchMovies(query: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE genre LIKE '%' || :genre || '%'")
    fun getMoviesByGenre(genre: String): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getMovieCount(): Int

    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    // Categories
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    // Watchlist
    @Query("SELECT m.* FROM movies m INNER JOIN watchlist w ON m.id = w.movieId WHERE w.userId = :userId ORDER BY w.addedTimestamp DESC")
    fun getWatchlistForUser(userId: String): Flow<List<MovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE userId = :userId AND movieId = :movieId LIMIT 1)")
    suspend fun isMovieInWatchlist(userId: String, movieId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(watchlist: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE userId = :userId AND movieId = :movieId")
    suspend fun removeFromWatchlist(userId: String, movieId: Int)

    // Watch History
    @Query("SELECT m.* FROM movies m INNER JOIN watch_history h ON m.id = h.movieId WHERE h.userId = :userId ORDER BY h.lastWatchedTimestamp DESC")
    fun getWatchHistoryForUser(userId: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM watch_history WHERE userId = :userId ORDER BY lastWatchedTimestamp DESC")
    fun getWatchHistoryEntitiesForUser(userId: String): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE userId = :userId AND movieId = :movieId LIMIT 1")
    suspend fun getWatchHistoryItem(userId: String, movieId: Int): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWatchHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE userId = :userId AND movieId = :movieId")
    suspend fun deleteWatchHistoryItem(userId: String, movieId: Int)

    @Query("DELETE FROM watch_history WHERE userId = :userId")
    suspend fun clearWatchHistoryForUser(userId: String)

    // Reviews/Comments
    @Query("SELECT * FROM reviews WHERE movieId = :movieId ORDER BY timestamp DESC")
    fun getReviewsForMovie(movieId: Int): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)
}
