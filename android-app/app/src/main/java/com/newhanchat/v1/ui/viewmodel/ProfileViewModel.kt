package com.newhanchat.v1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newhanchat.v1.data.api.ApiService
import com.newhanchat.v1.data.model.UserResponse
import com.newhanchat.v1.data.repository.AuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _profile = MutableStateFlow<UserResponse?>(null)
    val profile = _profile.asStateFlow()

    init {
        loadProfile() // Automatically loads when the user clicks the Profile tab!
    }

    private fun loadProfile() {
        viewModelScope.launch {
            // Get cached user ID
            val userId = authPreferences.userId.firstOrNull()
            if (userId != null) {
                try {
                    val response = apiService.getUserProfile(userId)
                    if (response.isSuccessful) {
                        _profile.value = response.body()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authPreferences.clearCredentials()
            onLogoutComplete()
        }
    }
}