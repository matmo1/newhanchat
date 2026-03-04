package com.newhanchat.v1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newhanchat.v1.data.api.TokenManager
import com.newhanchat.v1.data.model.AuthRequest
import com.newhanchat.v1.data.repository.AuthPreferences
import com.newhanchat.v1.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun login(username: String, password: String, onSuccess: (String, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.login(AuthRequest(username, password))

            result.onSuccess { response ->
                // 1. Save to global TokenManager for Retrofit interceptor
                TokenManager.token = response.token

                // 2. SAVE TO DEVICE CACHE!
                authPreferences.saveCredentials(response.token, response.userId)

                onSuccess(response.token, response.userId) // Navigate to Feed
            }
            result.onFailure { e ->
                _error.value = e.message ?: "Login failed"
            }

            _isLoading.value = false
        }
    }
}