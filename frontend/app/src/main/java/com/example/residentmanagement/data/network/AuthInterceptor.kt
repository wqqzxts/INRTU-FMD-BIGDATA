package com.example.residentmanagement.data.network

import com.example.residentmanagement.data.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url().encodedPath()

        if (path == "/api/refresh/") {
            val refreshToken = tokenManager.refreshToken ?: return chain.proceed(request)
            val newRequest = request.newBuilder()
                .addHeader("Cookie", "refresh=$refreshToken")
                .build()
            return chain.proceed(newRequest)
        }

        if (path in listOf("/api/login/", "/api/register/") || tokenManager.accessToken == null) {
            return chain.proceed(request)
        }

        val accessToken = tokenManager.accessToken ?: return chain.proceed(request)
        val finalRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(finalRequest)
    }
}