package com.newhanchat.demo.chatservices

import okhttp3.Interceptor
import okhttp3.Response

// 1. Token Manager
object TokenManager {
    var token: String? = null
}

// 2. Interceptor with DEBUG LOGS
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = TokenManager.token

        // --- DEBUG LOGGING ---
        if (token == null) {
            println("🛑 DEBUG: Token is NULL in TokenManager! Sending request without auth.")
        } else {
            println("✅ DEBUG: Attaching token: Bearer ${token.take(10)}...")
        }
        // ---------------------

        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}