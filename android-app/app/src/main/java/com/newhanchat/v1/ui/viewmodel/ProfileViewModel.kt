package com.newhanchat.v1.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newhanchat.v1.data.api.ApiService
import com.newhanchat.v1.data.model.UserResponse
import com.newhanchat.v1.data.repository.AuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _profile = MutableStateFlow<UserResponse?>(null)
    val profile = _profile.asStateFlow()

    // ✨ NEW: Track loading state to show/hide the spinner
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // ✨ NEW: Track errors to show retry messages
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadProfile() // Automatically loads when the user clicks the Profile tab!
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Get cached user ID
            val userId = authPreferences.userId.firstOrNull()

            if (userId != null) {
                try {
                    // Since ApiService returns UserResponse directly, we just assign it!
                    val userResponse = apiService.getUserProfile(userId)
                    _profile.value = userResponse

                } catch (e: retrofit2.HttpException) {
                    // Retrofit throws an HttpException for non-2xx responses (like 404, 500)
                    _error.value = "Failed to load profile: ${e.code()}"
                } catch (e: Exception) {
                    // Handle network errors (e.g., no internet connection, timeouts)
                    _error.value = "Network error. Please check your connection and try again."
                    e.printStackTrace()
                }
            } else {
                _error.value = "User not logged in."
            }

            _isLoading.value = false
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authPreferences.clearCredentials()
            onLogoutComplete()
        }
    }

    fun updateBio(newBio: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val userId = authPreferences.userId.firstOrNull()

            if (userId != null) {
                try {
                    // Convert the String into a plain text RequestBody for Retrofit
                    val requestBody = newBio.toRequestBody("text/plain".toMediaTypeOrNull())

                    // Call the API (Make sure your ApiService has this method!)
                    val updatedProfile = apiService.updateBio(userId, requestBody)

                    // Update the local state so the profile screen refreshes instantly
                    _profile.value = updatedProfile

                    // Trigger the navigation back to the profile screen
                    onSuccess()
                } catch (e: Exception) {
                    _error.value = "Failed to update bio. Please try again."
                    e.printStackTrace()
                }
            }
            _isLoading.value = false
        }
    }

    // 📸 Function to Upload Profile Picture
    fun uploadProfilePicture(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authPreferences.userId.firstOrNull()

            if (userId != null) {
                try {
                    // 1. Copy the secure Uri to a temporary file in the app's cache
                    val tempFile = File(context.cacheDir, "temp_profile_pic.jpg")
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val outputStream = FileOutputStream(tempFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    // 2. Prepare the file for Retrofit
                    val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                    // 3. Upload to backend
                    val updatedProfile = apiService.uploadProfilePicture(userId, body)
                    _profile.value = updatedProfile

                    // 4. Clean up temp file
                    tempFile.delete()

                } catch (e: Exception) {
                    _error.value = "Failed to upload image."
                    e.printStackTrace()
                }
            }
            _isLoading.value = false
        }
    }

    // 📝 Function to Update Name
    fun updateName(fname: String, lname: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authPreferences.userId.firstOrNull()
            if (userId != null) {
                try {
                    val updatedProfile = apiService.updateName(userId, fname, lname)
                    _profile.value = updatedProfile
                    onSuccess()
                } catch (e: Exception) {
                    _error.value = "Failed to update name."
                    e.printStackTrace()
                }
            }
            _isLoading.value = false
        }
    }
    fun saveBackgroundUri(uri: android.net.Uri) {
        viewModelScope.launch {
            authPreferences.saveBackgroundUri(uri.toString())
        }
    }
}