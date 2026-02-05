package com.newhanchat.v1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newhanchat.v1.data.repository.PostRepository
import com.newhanchat.v1.data.model.PostResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    // States
    private val _posts = MutableStateFlow<List<PostResponse>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _userMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val userMap = _userMap.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Load Users
            val userResult = repository.getUsers()
            userResult.onSuccess { users ->
                _userMap.value = users.associate { it.id to "${it.fname} ${it.lname}" }
            }

            // Load Posts
            val postResult = repository.getPosts()
            postResult.onSuccess { _posts.value = it }
            postResult.onFailure { _error.value = it.message }

            _isLoading.value = false
        }
    }

    fun createPost(content: String, imagePart: MultipartBody.Part?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            var uploadedUrl: String? = null

            // 1. Upload Image
            if (imagePart != null) {
                val uploadResult = repository.uploadImage(imagePart)
                uploadResult.onSuccess { uploadedUrl = it }
                uploadResult.onFailure {
                    _error.value = "Upload Failed: ${it.message}"
                    _isLoading.value = false
                    return@launch
                }
            }

            // 2. Create Post
            val postResult = repository.createPost(content, uploadedUrl)
            postResult.onSuccess {
                loadData() // Refresh list
                onSuccess()
            }
            postResult.onFailure { _error.value = "Post Failed: ${it.message}" }

            _isLoading.value = false
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            repository.deletePost(postId).onSuccess { loadData() }
        }
    }

    fun clearError() { _error.value = null }
}