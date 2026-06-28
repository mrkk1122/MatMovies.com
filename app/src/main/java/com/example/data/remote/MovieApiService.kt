package com.example.data.remote

import com.example.data.model.MovieEntity
import retrofit2.http.*

data class UserLoginRequest(val email: String, val password: String)
data class UserRegisterRequest(val id: String, val name: String, val email: String, val password: String)
data class UserResponse(val id: String, val name: String, val email: String, val subscription_status: String)

data class WatchlistRequest(val movie_id: Int)
data class ProgressRequest(val movie_id: Int, val progress_ms: Int, val duration_ms: Int)
data class MessageResponse(val message: String)

interface MovieApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: UserLoginRequest): UserResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: UserRegisterRequest): UserResponse

    @GET("api/users/{user_id}")
    suspend fun getUserProfile(@Path("user_id") userId: String): UserResponse

    @PUT("api/users/{user_id}/subscription")
    suspend fun upgradeSubscription(@Path("user_id") userId: String, @Query("plan") plan: String): UserResponse

    @GET("api/movies")
    suspend fun getMovies(
        @Query("genre") genre: String? = null,
        @Query("search") search: String? = null
    ): List<MovieEntity>

    @GET("api/movies/{movie_id}")
    suspend fun getMovieDetail(@Path("movie_id") movieId: Int): MovieEntity

    @POST("api/movies")
    suspend fun createMovie(@Body movie: MovieEntity): MovieEntity

    @DELETE("api/movies/{movie_id}")
    suspend fun deleteMovie(@Path("movie_id") movieId: Int): MessageResponse

    @GET("api/users/{user_id}/watchlist")
    suspend fun getWatchlist(@Path("user_id") userId: String): List<MovieEntity>

    @POST("api/users/{user_id}/watchlist")
    suspend fun addToWatchlist(@Path("user_id") userId: String, @Body request: WatchlistRequest): MessageResponse

    @DELETE("api/users/{user_id}/watchlist/{movie_id}")
    suspend fun removeFromWatchlist(@Path("user_id") userId: String, @Path("movie_id") movieId: Int): MessageResponse

    @POST("api/users/{user_id}/progress")
    suspend fun saveProgress(@Path("user_id") userId: String, @Body request: ProgressRequest): MessageResponse
}
