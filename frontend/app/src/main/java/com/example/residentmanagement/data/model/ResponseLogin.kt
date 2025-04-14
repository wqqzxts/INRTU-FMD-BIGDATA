package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName

data class ResponseLogin(
    @SerializedName("access") val accessToken: String,
    @SerializedName("is_staff") val isStaff: Boolean
)
