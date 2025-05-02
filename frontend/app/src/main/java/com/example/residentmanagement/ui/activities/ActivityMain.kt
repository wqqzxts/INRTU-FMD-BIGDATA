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
import com.example.residentmanagement.data.model.ResponseLogin
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private fun authenticate() {
        Toast.makeText(
            this@ActivityMain,
            "Вход произведен успешно",
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(this, ActivityHome::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun authAttempt() {
        lifecycleScope.launch {
            try {
                val isValid = authManager.accessToken?.let { _ ->
                    withContext(Dispatchers.IO) {
                        isTokenValid()
                    }
                } ?: false

                if (isValid) {
                    authenticate()
                } else {
                    authManager.clearAuthCredentials()
                    Toast.makeText(this@ActivityMain, "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ActivityMain GET token validation", "Error validating token: ${e.message}")
            }
        }
    }

    private suspend fun isTokenValid(): Boolean {
        return try {
            RetrofitClient.getApiService().validateToken().isSuccessful
        } catch (e: Exception) {
            Log.e("ActivityMain GET token validation", "Error validating token: ${e.message}")
            false
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
            val result = kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitClient.getApiService().loginUser(request)
                }
            }

            result.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let { tokens ->
                        handleSuccessfulLogin(response.headers()["Set-Cookie"], tokens)
                    } ?: run {
                        Log.e("ActivityMain POST login user", "Empty body in response")
                    }
                }
            }.onFailure { e ->
                Log.e("ActivityMain POST user login", "Error: ${e.message}", e)
            }
        }
    }

    private suspend fun handleSuccessfulLogin(cookies: String?, responseLogin: ResponseLogin) {
        withContext(Dispatchers.Main) {
            val extractedRefreshToken = extractRefreshToken(cookies)
            authManager.apply {
                accessToken = responseLogin.accessToken
                refreshToken = extractedRefreshToken
                isStaff = responseLogin.user.isStaff
            }

            authenticate()
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
}