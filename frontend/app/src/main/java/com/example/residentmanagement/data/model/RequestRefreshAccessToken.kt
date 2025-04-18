package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RequestRefreshAccessToken(
    @SerializedName("refresh") val refreshToken: String
) : Serializable