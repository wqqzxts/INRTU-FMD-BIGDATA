package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName

data class RequestEditUser(
    @SerializedName("first_name") val firstName : String,
    @SerializedName("last_name") val lastName : String,
    val gender: Char,
    val apartments: Int,
    val email: String,
    val password: String
)