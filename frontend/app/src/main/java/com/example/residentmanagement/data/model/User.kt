package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    @SerializedName("first_name") val firstName : String,
    @SerializedName("last_name")  val lastName : String,
    val gender: String,
    val apartments: Int,
    val email: String,
    @SerializedName("is_staff") val isStaff: Boolean
) : Serializable