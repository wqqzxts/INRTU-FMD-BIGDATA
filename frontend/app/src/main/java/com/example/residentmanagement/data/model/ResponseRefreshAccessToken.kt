package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResponseRefreshAccessToken(
    @SerializedName("access") val accessToken: String
) : Serializable