package com.example.residentmanagement.data.network

import android.content.Context
import com.example.residentmanagement.data.util.TokenAuthenticator
import com.example.residentmanagement.data.util.AuthManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private var authManager: AuthManager? = null
    private var retrofit: Retrofit? = null

    fun initialize(context: Context) {
        authManager = AuthManager(context)

        val okHttpClient  = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authManager!!))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        val authenticatedClient = okHttpClient.newBuilder()
            .authenticator(TokenAuthenticator(authManager!!, retrofit!!.create(ApiService::class.java)))
            .build()

        retrofit = retrofit?.newBuilder()?.client(authenticatedClient)?.build()
    }

    fun getApiService(): ApiService {
        return retrofit!!.create(ApiService::class.java)
    }
}