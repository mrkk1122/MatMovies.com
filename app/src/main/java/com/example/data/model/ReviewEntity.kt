package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val username: String,
    val movieId: Int,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
