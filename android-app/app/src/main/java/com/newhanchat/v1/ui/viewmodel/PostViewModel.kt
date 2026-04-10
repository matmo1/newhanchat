package com.newhanchat.v1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newhanchat.v1.data.api.ApiService
import com.newhanchat.v1.data.model.PostRequest
import com.newhanchat.v1.data.model.PostResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostResponse>>(emptyList())
    val posts: StateFlow<List<PostResponse>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getPosts(page = 0, size = 20)
                if (response.isSuccessful) {
                    _posts.value = response.body()?.content ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✨ FIXED: Added authorName and authorProfilePic
    fun createPost(
        content: String,
        imagePart: MultipartBody.Part?,
        authorName: String,
        authorProfilePic: String,
        onSuccess: () -> Unit
    ) {
        _isUploading.value = true
        viewModelScope.launch {
            try {
                var imageUrl = ""

                if (imagePart != null) {
                    val uploadResponse = apiService.uploadImage(imagePart)
                    if (uploadResponse.isSuccessful) {
                        imageUrl = uploadResponse.body() ?: ""
                    }
                }

                // ✨ FIXED: Attach the author info to the request!
                val postRequest = PostRequest(
                    content = content,
                    imageUrl = imageUrl,
                    authorName = authorName,
                    authorProfilePic = authorProfilePic
                )
                val createResponse = apiService.createPost(postRequest)

                if (createResponse.isSuccessful) {
                    loadPosts()
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.deletePost(postId)
                if (response.isSuccessful) {
                    loadPosts() // Refresh the feed after deleting
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}