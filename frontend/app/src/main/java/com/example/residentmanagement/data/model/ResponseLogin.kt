package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResponseLogin(
    @SerializedName("access") val accessToken: String,
    val user: User
) : Serializable
