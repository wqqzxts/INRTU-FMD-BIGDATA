package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName

data class RequestRegister(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val gender: String,
    val apartments: Int?,
    val email: String,
    val password: String
)
