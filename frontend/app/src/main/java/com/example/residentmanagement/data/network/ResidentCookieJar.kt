package com.example.residentmanagement.data.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class ResidentCookieJar : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
        TODO("Not yet implemented")
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        TODO("Not yet implemented")
    }
}