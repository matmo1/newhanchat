package com.newhanchat.v1.data.api

import com.newhanchat.v1.BuildConfig
import com.newhanchat.v1.data.model.AuthRequest
import com.newhanchat.v1.data.model.ChatMessageDTO
import com.newhanchat.v1.data.model.JwtResponse
import com.newhanchat.v1.data.model.PagedResponse
import com.newhanchat.v1.data.model.PostRequest
import com.newhanchat.v1.data.model.PostResponse
import com.newhanchat.v1.data.model.RegisterRequest
import com.newhanchat.v1.data.model.UserResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {
    @POST("/api/users/login")
    suspend fun login(@Body request: AuthRequest): Response<JwtResponse>

    @POST("/api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @GET("/api/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): UserResponse

    // Update the bio. Spring expects raw text, so we use RequestBody in Retrofit
    @PATCH("api/users/{id}/bio")
    suspend fun updateBio(
        @Path("id") userId: String,
        @Body newBio: RequestBody
    ): UserResponse

    @GET("/api/users")
    suspend fun getUsers(): Response<List<UserResponse>>

    // FIXED: Typo "messagages" -> "messages"
    @GET("/api/messages/history")
    suspend fun getHistory(
        @Query("senderId") senderId: String,
        @Query("recipientId") recipientId: String
    ): Response<List<ChatMessageDTO>>

    @PATCH("/api/users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") userId: String,
        @Query("status") status: String
    ): Response<UserResponse>

    @GET("/api/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PagedResponse<PostResponse>>

    // --- STEP 1: Upload the file ---
    @Multipart
    @POST("/api/posts/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<String>

    // --- STEP 2: Create the post with the text and image URL ---
    @POST("/api/posts")
    suspend fun createPost(
        @Body request: PostRequest
    ): Response<PostResponse>

    @DELETE("/api/posts/{id}")
    suspend fun deletePost(@Path("id") postId: Long): Response<Void>

    // Upload Profile Picture (Multipart)
    @Multipart
    @POST("api/users/{id}/profile-picture")
    suspend fun uploadProfilePicture(
        @Path("id") userId: String,
        @Part file: MultipartBody.Part
    ): UserResponse

    // Update Name
    @PATCH("api/users/{id}/name")
    suspend fun updateName(
        @Path("id") userId: String,
        @Query("fname") fname: String,
        @Query("lname") lname: String
    ): UserResponse
}

// Ensure this matches your computer's IP

val apiService: ApiService by lazy {
    val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}