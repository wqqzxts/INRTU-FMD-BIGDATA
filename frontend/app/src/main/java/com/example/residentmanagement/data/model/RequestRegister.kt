package com.example.residentmanagement.data.model

data class RequestRegister(
    val first_name: String,
    val lastName: String,
    val gender: String,
    val apartments: Int?,
    val email: String,
    val password: String
)
