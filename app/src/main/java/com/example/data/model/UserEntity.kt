package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // UUID or email
    val email: String,
    val passwordHash: String,
    val username: String,
    val role: String = "User", // "User" or "Admin"
    val subscriptionStatus: String = "Free", // "Free", "Premium", "VIP"
    val createdAt: Long = System.currentTimeMillis()
)
