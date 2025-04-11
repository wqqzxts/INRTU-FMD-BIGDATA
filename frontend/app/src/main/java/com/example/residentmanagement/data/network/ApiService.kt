package com.example.residentmanagement.data.network

import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.data.model.RequestCreateEditPublication
import com.example.residentmanagement.data.model.RequestLogin
import com.example.residentmanagement.data.model.RequestRegister

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ApiService  {
    // user login endpoint
    @POST("/api/login/")
    fun userLogin(@Body request: RequestLogin): Call<Void>

    // user registration endpoint
    @POST("/api/register/")
    fun userRegister(@Body request: RequestRegister): Call<Void>

    // publication create endpoint
    @POST("/api/publications/")
    fun createPublication(@Body request: RequestCreateEditPublication): Call<Void>

    // publications list endpoint
    @GET("/api/publications/")
    fun getPublications(): Call<List<Publication>>

    // specific publication info endpoint
    @GET("/api/publications/{publication_id}")
    fun getSpecificPublication(@Path("publication_id") publicationId: Int): Call<Publication>

    // specific publication edit endpoint
    @PATCH("/api/publications/{publication_id}/")
    fun updateSpecificPublication(
        @Path("publication_id") publicationId: Int,
        @Body request: RequestCreateEditPublication
    ): Call<Publication>

    // specific publication delete endpoint
    @DELETE("api/publications/{publication_id}/")
    fun deleteSpecificPublication(@Path("publication_id") publicationId: Int): Call<Void>
}