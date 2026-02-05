package com.newhanchat.v1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newhanchat.v1.data.api.TokenManager
import com.newhanchat.v1.data.model.AuthRequest
import com.newhanchat.v1.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
                // Save Token Globally
                TokenManager.token = response.token
                onSuccess(response.token, response.userId)
            }
            result.onFailure { e ->
                _error.value = e.message ?: "Login failed"
            }

            _isLoading.value = false
        }
    }
}