package com.example.residentmanagement.data.network

import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.data.model.RequestCreateEditPublication
import com.example.residentmanagement.data.model.RequestEditUser
import com.example.residentmanagement.data.model.RequestLogin
import com.example.residentmanagement.data.model.RequestRegister
import com.example.residentmanagement.data.model.ResponseLogin
import com.example.residentmanagement.data.model.ResponseRefreshAccessToken
import com.example.residentmanagement.data.model.User

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ApiService  {
    @POST("/api/v1/auth/register/")
    suspend fun registerUser(@Body request: RequestRegister): Response<Void>

    @POST("/api/v1/auth/login/")
    suspend fun loginUser(@Body request: RequestLogin): Response<ResponseLogin>

    @POST("/api/v1/auth/logout/")
    suspend fun logoutUser(): Response<Void>

    @POST("/api/v1/auth/token/refresh/")
    fun refreshTokenSync(): Call<ResponseRefreshAccessToken>

    @GET("/api/v1/auth/token/validate/")
    suspend fun validateToken(): Response<Void>

    @POST("/api/v1/publications/")
    suspend fun createPublication(@Body request: RequestCreateEditPublication): Response<Void>

    @GET("/api/v1/publications/")
    suspend fun getPublications(): Response<List<Publication>>

    @GET("/api/v1/publications/{publication_id}/")
    suspend fun getSpecificPublication(@Path("publication_id") publicationId: Int): Response<Publication>

    @PATCH("/api/v1/publications/{publication_id}/")
    suspend fun updateSpecificPublication(
        @Path("publication_id") publicationId: Int,
        @Body request: RequestCreateEditPublication
    ): Response<Publication>

    @DELETE("/api/v1/publications/{publication_id}/")
    suspend fun deleteSpecificPublication(@Path("publication_id") publicationId: Int): Response<Void>

    @GET("/api/v1/profile/")
    suspend fun getProfileInfo(): Response<User>

    @PATCH("/api/v1/profile/edit/")
    suspend fun updateProfileInfo(@Body request: RequestEditUser): Response<Void>
}