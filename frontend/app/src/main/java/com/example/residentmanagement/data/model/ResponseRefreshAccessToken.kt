package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName

data class ResponseRefreshAccessToken(
    @SerializedName("access") val accessToken: String
)