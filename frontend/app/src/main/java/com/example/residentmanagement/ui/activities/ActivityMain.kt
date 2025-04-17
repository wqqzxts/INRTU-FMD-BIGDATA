package com.example.residentmanagement.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.residentmanagement.R

import com.example.residentmanagement.data.model.RequestLogin
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import kotlinx.coroutines.launch

class ActivityMain : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)
        authManager = AuthManager(this)

        if (!authManager.isSessionExpiredFromApp) {
            authAttempt()
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        emailInput = findViewById(R.id.email_login)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.toRegister_button)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginButton.setOnClickListener {
            login()
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, ActivityRegistration::class.java))
        }
    }

    private fun login() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, укажите все поля формы.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RequestLogin(email, password)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().loginUser(request)

                if (response.code() == 200) {
                    val tokens = response.body()
                    if (tokens != null) {
                        authManager.accessToken = tokens.accessToken

                        val cookies = response.headers()["Set-Cookie"]
                        val refreshToken = extractRefreshToken(cookies)

                        authManager.refreshToken = refreshToken
                        authManager.accessToken = tokens.accessToken
                        authManager.isStaff = tokens.user.isStaff

                        Toast.makeText(
                            this@ActivityMain,
                            "Вход произведен успешно",
                            Toast.LENGTH_SHORT
                        ).show()
                        successAuthHandler()
                    } else {
                        Log.e("ActivityMain POST login user", "Empty body in response")
                    }
                } else {
                    Toast.makeText(this@ActivityMain, "Неверные данные", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ActivityMain POST login user", "Error: ${e.message}", e)
            }
        }
    }

    private fun extractRefreshToken(cookies: String?): String? {
        if (cookies.isNullOrEmpty()) return null

        return cookies.split(";")
            .map { it.trim() }
            .firstOrNull { it.startsWith("refresh=") }
            ?.substringAfter("=")
            ?.takeIf { it.isNotBlank() }
    }

    private fun authAttempt() {
        lifecycleScope.launch {
            if (authManager.accessToken != null && isTokenValid()) {
                successAuthHandler()
            } else {
                Toast.makeText(this@ActivityMain, "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                return@launch
            }
        }
    }

    private suspend fun isTokenValid(): Boolean {
        return try {
            val response = RetrofitClient.getApiService().validateToken()

            when (response.code()) {
                200 -> true
                401 -> false
                else -> {
                    Log.w("ActivityMain GET token validation", "Response status code didn't handle: ${response.code()}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("ActivityMain GET token validation", "Error validating token: ${e.message}")
            false
        }
    }

    private fun successAuthHandler() {
        startActivity(Intent(this, ActivityHome::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}