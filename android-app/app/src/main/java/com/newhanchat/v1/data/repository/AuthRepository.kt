package com.newhanchat.v1.data.repository

import com.newhanchat.v1.data.api.ApiService
import com.newhanchat.v1.data.model.AuthRequest
import com.newhanchat.v1.data.model.JwtResponse
import com.newhanchat.v1.data.model.RegisterRequest
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun login(request: AuthRequest): Result<JwtResponse> {
        return safeApiCall { apiService.login(request) }
    }

    suspend fun register(request: RegisterRequest): Result<Unit> {
        return safeApiCall { apiService.register(request) }
            .map { } // Convert Result<Void> to Result<Unit>
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null || response.code() == 200) {
                    Result.success(body as T)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}