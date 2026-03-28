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

    // Pagination Trackers
    private var currentPage = 0
    private var isLastPage = false
    private var isFetchingPosts = false

    init {
        loadInitialData()
    }

    // Resets the page count and loads the very first batch
    fun loadInitialData() {
        currentPage = 0
        isLastPage = false
        _posts.value = emptyList()

        viewModelScope.launch {
            _isLoading.value = true

            // Load Users (Only really needed once)
            if (_userMap.value.isEmpty()) {
                val userResult = repository.getUsers()
                userResult.onSuccess { users ->
                    _userMap.value = users.associate { it.id to "${it.fname} ${it.lname}" }
                }
            }

            _isLoading.value = false

            // Start fetching first page
            loadMorePosts()
        }
    }

    // Fetches pages chunks as the user scrolls
    fun loadMorePosts() {
        // Prevent duplicate network calls or loading past the end
        if (isFetchingPosts || isLastPage) return

        viewModelScope.launch {
            isFetchingPosts = true

            val postResult = repository.getPosts(currentPage, 10)

            postResult.onSuccess { pagedData ->
                // Extract the actual list of posts from the "content"
                val newPosts = pagedData.content

                // Append the new page to our existing list
                _posts.value = _posts.value + newPosts

                // Update trackers
                isLastPage = pagedData.last
                if (!isLastPage) {
                    currentPage++
                }
            }
            postResult.onFailure { _error.value = it.message }

            isFetchingPosts = false
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
            postResult.onSuccess { newPost ->
                // ✨ FIX: Prepend the newly created post locally without wiping the feed!
                _posts.value = listOf(newPost) + _posts.value
                onSuccess()
            }
            postResult.onFailure { _error.value = "Post Failed: ${it.message}" }

            _isLoading.value = false
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            repository.deletePost(postId).onSuccess {
                // ✨ FIX: Remove the post locally without wiping the feed!
                _posts.value = _posts.value.filter { it.id != postId }
            }
            repository.deletePost(postId).onFailure {
                _error.value = "Failed to delete post: ${it.message}"
            }
        }
    }

    fun clearError() { _error.value = null }
}