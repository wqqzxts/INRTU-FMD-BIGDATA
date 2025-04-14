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
import com.example.residentmanagement.data.model.RequestRefreshAccessToken
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)
        authManager = AuthManager(this)

        authAttempt()

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
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun login() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, укажите и почту и пароль.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RequestLogin(email, password)

        lifecycleScope.launch {
            try {
                val loginResponse = RetrofitClient.getApiService().loginUser(request)
                if (loginResponse.isSuccessful) {
                    val response = loginResponse.body()!!
                    authManager.accessToken = response.accessToken

                    val cookies = loginResponse.headers()["Set-Cookie"]
                    val refreshToken = refreshTokenFromCookie(cookies)

                    if (refreshToken != null) {
                        authManager.refreshToken = refreshToken
                    } else {
                        Log.e("LOGIN", "Refresh token was not found in cookies")
                    }

                    authManager.accessToken = response.accessToken

                    authManager.isStaff = response.user.isStaff

                    Toast.makeText(this@MainActivity, "Вход произведен успешно", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    finish()
                } else {
                    val errorBody = loginResponse.errorBody()?.string()
                    Log.e("LOGIN", "Error: $errorBody")
                    Toast.makeText(this@MainActivity, "Ошибка входа: $errorBody", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LOGIN", "Error: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshTokenFromCookie(cookies: String?): String? {
        if (cookies.isNullOrEmpty()) return null

        return cookies.split(";")
            .firstOrNull() { it.trim().startsWith("refresh=") }
            ?.substringAfter("=")
            ?.trim()
    }

    private fun authAttempt() {
        when {
            authManager.accessToken != null -> {
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                finish()
            }
            authManager.refreshToken != null -> {
                refreshTokenAttempt()
            }
            else -> {
                return
            }
        }
    }

    private fun refreshTokenAttempt() {
        lifecycleScope.launch {
            try {
                val refreshToken = authManager.refreshToken ?: return@launch
                val refreshRequest = RequestRefreshAccessToken(refreshToken)
                val refreshResponse = RetrofitClient.getApiService().refreshToken(refreshRequest)

                if (refreshResponse.isSuccessful) {
                    val response = refreshResponse.body()
                    authManager.accessToken = response!!.accessToken
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    finish()
                } else {
                    authManager.clearTokens()
                    Toast.makeText(this@MainActivity, "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            } catch (e: Exception) {
                Log.e("REFRESH", "Error refreshing token: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                authManager.clearTokens()
                return@launch
            }
        }
    }
}