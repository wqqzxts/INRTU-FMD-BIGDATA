package com.example.residentmanagement

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class ResidentManagement : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
    }
}