package com.example.residentmanagement.data.network

import com.example.residentmanagement.data.util.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authManager: AuthManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url().encodedPath()

        if (path == "/api/v1/auth/token/refresh/") {
            val refreshToken = authManager.refreshToken ?: return chain.proceed(request)
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "Bearer $refreshToken")
                .build()
            return chain.proceed(newRequest)
        }

        if (path in listOf("/api/v1/auth/login/", "/api/v1/auth/register/") || authManager.accessToken == null) {
            return chain.proceed(request)
        }

        val accessToken = authManager.accessToken ?: return chain.proceed(request)
        val finalRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(finalRequest)
    }
}