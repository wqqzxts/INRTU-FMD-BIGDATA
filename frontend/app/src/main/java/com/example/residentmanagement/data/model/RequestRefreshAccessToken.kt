package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName

data class RequestRefreshAccessToken(
    @SerializedName("refresh") val refreshToken: String
)