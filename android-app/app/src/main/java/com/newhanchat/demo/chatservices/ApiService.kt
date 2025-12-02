package com.newhanchat.demo.chatservices

import com.newhanchat.demo.loginandregister.*
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/api/users/login")
    suspend fun login(@Body request: AuthRequest): Response<JwtResponse>

    @POST("/api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @GET("/api/users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("/api/messagages/history")
    suspend fun getHistory(
        @Query("senderId") senderId: String,
        @Query("recipientId") recipientId: String
    ): Response<List<ChatMessageDTO>>

    @retrofit2.http.PATCH("/api/users/{id}/status")
    suspend fun updateUserStatus(
        @retrofit2.http.Path("id") userId: String,
        @retrofit2.http.Query("status") status: String // ONLINE, BUSY, etc.
    ): Response<UserResponse>
}

// CHANGE THIS IP to your computer's IP
private const val BASE_URL = "http://10.143.133.97:8080"

val apiService: ApiService by lazy {
    // 1. Create Client with Interceptor
    val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor()) // <--- THIS IS CRITICAL
        .build()

    // 2. Build Retrofit
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client) // <--- YOU MUST HAVE THIS LINE
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}