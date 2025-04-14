package com.example.residentmanagement.data.util

import android.content.Context

class TokenManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var accessToken: String?
        get() = sharedPreferences.getString("access", null)
        set(value) {
            sharedPreferences.edit().putString("access", value).apply()
        }

    var refreshToken: String?
        get() = sharedPreferences.getString("refresh", null)
        set(value) {
            sharedPreferences.edit().putString("refresh", value).apply()
        }

    fun clearTokens() {
        sharedPreferences.edit()
            .remove("access")
            .remove("refresh")
            .apply()
    }
}