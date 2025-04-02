package com.example.residentmanagement.data.network

import com.example.residentmanagement.data.model.LoginRequest
import com.example.residentmanagement.data.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.DELETE

interface ApiService  {
    // user login endpoint
    @POST("/api/login/")
    fun userLogin(@Body request: LoginRequest): Call<Void>

    // user registration endpoint
    @POST("/api/register/")
    fun userRegister(@Body request: RegisterRequest): Call<Void>
}