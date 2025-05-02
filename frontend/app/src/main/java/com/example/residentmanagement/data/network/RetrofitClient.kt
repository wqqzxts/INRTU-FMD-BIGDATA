package com.example.residentmanagement.data.network

import android.content.Context
import com.example.residentmanagement.data.util.AuthManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val TIMEOUT_SEC = 5L
    private lateinit var authManager: AuthManager
    private lateinit var retrofit: Retrofit

    fun initialize(context: Context) {
        authManager = AuthManager(context)

        val okHttpClient  = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authManager))
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        val authenticatedClient = okHttpClient.newBuilder()
            .authenticator(AuthToken(authManager, retrofit.create(ApiService::class.java)))
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()

        retrofit = retrofit.newBuilder()
            .client(authenticatedClient)
            .build()
    }

    fun getApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}