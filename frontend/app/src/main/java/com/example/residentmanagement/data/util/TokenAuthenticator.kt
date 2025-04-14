package com.example.residentmanagement.data.util

import com.example.residentmanagement.data.model.RequestRefreshAccessToken
import com.example.residentmanagement.data.network.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authManager: AuthManager,
    private val apiService: ApiService
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request().header("X-Retry") != null) {
            return null
        }

        val refreshToken = authManager.refreshToken ?: return null

        return runBlocking {
            try {
                val refreshRequest = RequestRefreshAccessToken(refreshToken)
                val refreshResponse = apiService.refreshToken(refreshRequest)
                val refreshResponseBody = refreshResponse.body()
                val refreshedAccessToken = refreshResponseBody!!.accessToken

                synchronized(this) {
                    authManager.accessToken = refreshedAccessToken
                }

                response.request().newBuilder()
                    .header("Authorization", "Bearer $refreshedAccessToken")
                    .header("X-Retry", "1")
                    .build()
            } catch (e: Exception) {
                authManager.clearTokens()
                null
            }
        }
    }
}