package com.example.residentmanagement.data.util

import android.content.Context

class AuthManager(context: Context) {
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

    var isStaff: Boolean
        get() = sharedPreferences.getBoolean("is_staff", false)
        set(value) = sharedPreferences.edit().putBoolean("is_staff", value).apply()

    var isSessionExpiredFromApp: Boolean
        get() = sharedPreferences.getBoolean("is_session_expired_from_app", false)
        set(value) = sharedPreferences.edit().putBoolean("is_session_expired_from_app", value).apply()

    fun clearAuthCredentials() {
        sharedPreferences.edit()
            .remove("access")
            .remove("refresh")
            .putBoolean("is_session_expired_from_app", false)
            .putBoolean("is_staff", false)
            .apply()
    }
}