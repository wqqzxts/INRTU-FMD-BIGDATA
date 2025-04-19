package com.example.residentmanagement.data.local

import android.content.Context
import android.util.Log
import com.example.residentmanagement.data.model.User
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class CacheManager(private val context: Context) {
    private val USER_PROFILE = "user_profile.dat"

    fun saveUserData(userData: User) {
        try {
            val fileOutputStream = context.openFileOutput(USER_PROFILE, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(userData)
            fileOutputStream.close()
            objectOutputStream.close()
        } catch (e: IOException) {
            Log.e("CacheManager:", "failed to save user profile ${e.message}")
        }
    }

    fun loadUserData(): User? {
        return try {
            val fileInputStream = context.openFileInput(USER_PROFILE)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val userData = objectInputStream.readObject() as User
            objectInputStream.close()
            fileInputStream.close()
            userData
        } catch (e: FileNotFoundException) {
            null
        } catch (e: IOException) {
            Log.e("CacheManager:", "failed to load user profile ${e.message}")
            null
        } catch (e: ClassNotFoundException) {
            Log.e("CacheManager:", "failed to cast user profile ${e.message}")
            null
        }
    }

    fun clearUserData() {
        context.deleteFile(USER_PROFILE)
    }
}