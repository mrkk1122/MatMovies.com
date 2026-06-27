package com.example.data.repository

import android.content.Context
import com.example.data.local.UserDao
import com.example.data.model.UserEntity
import java.security.MessageDigest
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("moviesbox_session", Context.MODE_PRIVATE)

    // Save and check logged in state
    fun setSession(userId: String, email: String, role: String, sub: String) {
        sharedPrefs.edit()
            .putString("current_user_id", userId)
            .putString("current_user_email", email)
            .putString("current_user_role", role)
            .putString("current_user_subscription", sub)
            .apply()
    }

    fun getCurrentUserId(): String? = sharedPrefs.getString("current_user_id", null)
    fun getCurrentUserEmail(): String? = sharedPrefs.getString("current_user_email", null)
    fun getCurrentUserRole(): String? = sharedPrefs.getString("current_user_role", null)
    fun getCurrentUserSubscription(): String? = sharedPrefs.getString("current_user_subscription", "Free")

    fun clearSession() {
        sharedPrefs.edit().clear().apply()
    }

    // Hash passwords using SHA-256 for secure local accounts
    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            password // fallback
        }
    }

    suspend fun registerUser(email: String, password: String, username: String): Result<UserEntity> {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            return Result.failure(Exception("Email already registered"))
        }

        // Standard user role, unless it's a specific admin-themed email (e.g., admin@moviesbox.com)
        val role = if (email.lowercase() == "admin@moviesbox.com") "Admin" else "User"
        val user = UserEntity(
            id = java.util.UUID.randomUUID().toString(),
            email = email,
            passwordHash = hashPassword(password),
            username = username,
            role = role,
            subscriptionStatus = if (role == "Admin") "VIP" else "Free"
        )
        userDao.insertUser(user)
        return Result.success(user)
    }

    suspend fun loginUser(email: String, password: String): Result<UserEntity> {
        val user = userDao.getUserByEmail(email) ?: return Result.failure(Exception("Email not found"))
        if (user.passwordHash != hashPassword(password)) {
            return Result.failure(Exception("Incorrect password"))
        }
        setSession(user.id, user.email, user.role, user.subscriptionStatus)
        return Result.success(user)
    }

    suspend fun updateSubscription(userId: String, tier: String): Result<Boolean> {
        val user = userDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        val updated = user.copy(subscriptionStatus = tier)
        userDao.updateUser(updated)
        
        // If current session is this user, update shared prefs too
        if (getCurrentUserId() == userId) {
            sharedPrefs.edit().putString("current_user_subscription", tier).apply()
        }
        return Result.success(true)
    }

    suspend fun promoteToAdmin(userId: String): Result<Boolean> {
        val user = userDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        val updated = user.copy(role = "Admin")
        userDao.updateUser(updated)
        return Result.success(true)
    }

    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
}
