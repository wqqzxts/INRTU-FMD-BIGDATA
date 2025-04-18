package com.example.residentmanagement.data.network

import com.example.residentmanagement.data.util.AuthManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthToken(
    private val authManager: AuthManager,
    private val apiService: ApiService
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request().header("X-Retry") != null) { return null }
        val call = apiService.refreshTokenSync()

        return try {
            val refreshResponse = call.execute()
            val refreshedAccessToken = refreshResponse.body()!!.accessToken
            synchronized(authManager) {
                authManager.accessToken = refreshedAccessToken
            }

            response.request().newBuilder()
                .header("Authorization", "Bearer $refreshedAccessToken")
                .header("X-Retry", "1")
                .build()
        } catch (e: Exception) {
            authManager.clearAuthCredentials()
            null
        }
    }
}