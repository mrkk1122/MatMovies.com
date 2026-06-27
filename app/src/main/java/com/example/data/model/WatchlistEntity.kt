package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val movieId: Int,
    val addedTimestamp: Long = System.currentTimeMillis()
)
