package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val firstName : String,
    val lastName : String,
    val email: String,
    @SerializedName("is_staff") val isStaff: Boolean
)
