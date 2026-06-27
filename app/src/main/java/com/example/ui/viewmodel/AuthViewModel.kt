package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserEntity
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerSuccess = MutableStateFlow<Boolean>(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()

    init {
        // Load session if user is already logged in
        checkCurrentSession()
    }

    private fun checkCurrentSession() {
        val userId = userRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    _currentUser.value = user
                } else {
                    userRepository.clearSession()
                }
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loginError.value = null
            userRepository.loginUser(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    onSuccess()
                }
                .onFailure { exception ->
                    _loginError.value = exception.message ?: "Authentication failed"
                }
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _registerError.value = null
            _registerSuccess.value = false
            userRepository.registerUser(email, password, username)
                .onSuccess {
                    _registerSuccess.value = true
                }
                .onFailure { exception ->
                    _registerError.value = exception.message ?: "Registration failed"
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            userRepository.clearSession()
            _currentUser.value = null
            onSuccess()
        }
    }

    fun upgradeSubscription(tier: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            userRepository.updateSubscription(user.id, tier)
                .onSuccess {
                    _currentUser.value = user.copy(subscriptionStatus = tier)
                }
        }
    }

    fun clearErrors() {
        _loginError.value = null
        _registerError.value = null
        _registerSuccess.value = false
    }
}
