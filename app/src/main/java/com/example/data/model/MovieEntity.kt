package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val rating: Double,
    val year: Int,
    val duration: String,
    val genre: String, // e.g., "Sci-Fi, Action"
    val posterDrawableName: String, // mapped to local drawables, e.g. "img_poster_cyberpunk"
    val videoUrl: String, // public MP4/HLS streaming link
    val viewsCount: Int = 0,
    val isTrending: Boolean = false,
    val isFeatured: Boolean = false,
    val isLatest: Boolean = false,
    val language: String = "English",
    val subtitlesUrl: String = "",
    val isUploadedByUser: Boolean = false,
    val isUpcoming: Boolean = false
)
