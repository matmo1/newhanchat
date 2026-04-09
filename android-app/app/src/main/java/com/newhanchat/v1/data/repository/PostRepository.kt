package com.newhanchat.v1.data.repository

import com.newhanchat.v1.data.api.ApiService
import com.newhanchat.v1.data.model.PagedResponse
import com.newhanchat.v1.data.model.PostResponse
import com.newhanchat.v1.data.model.PostRequest
import com.newhanchat.v1.data.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response

class PostRepository(private val apiService: ApiService) {

    suspend fun getPosts(page: Int, size: Int): Result<PagedResponse<PostResponse>> {
        return try {
            val response = apiService.getPosts(page, size)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(
        content: String,
        imageUrl: String,
        authorName: String,
        authorProfilePic: String
    ): Response<PostResponse> {
        // ✨ FIXED: Pass all 4 variables into the PostRequest
        val request = PostRequest(
            content = content,
            imageUrl = imageUrl,
            authorName = authorName,
            authorProfilePic = authorProfilePic
        )
        return apiService.createPost(request)
    }

    suspend fun uploadImage(file: MultipartBody.Part): Result<String> {
        return safeApiCall { apiService.uploadImage(file) }
    }

    suspend fun deletePost(id: Long): Result<Void> {
        return safeApiCall { apiService.deletePost(id) }
    }

    suspend fun getUsers(): Result<List<UserResponse>> {
        return safeApiCall { apiService.getUsers() }
    }

    // Helper to handle Try/Catch/HTTP Errors in one place
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null || response.code() == 200) {
                    Result.success(body as T)
                } else {
                    Result.failure(Exception("Body is null"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}