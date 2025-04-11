package com.example.residentmanagement.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Publication (
    val id: Int,
    val title: String,
    val content: String,
    @SerializedName("date_published") val datePublished: Date,
    val user: User
)