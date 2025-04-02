package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("apartments") val apartments: Int?,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
