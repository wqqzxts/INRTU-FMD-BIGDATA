package com.example.residentmanagement.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.residentmanagement.R

import com.example.residentmanagement.data.model.RequestRegister
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.residentmanagement.data.model.RequestLogin
import com.example.residentmanagement.data.model.ResponseLogin
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

class ActivityRegistration : AppCompatActivity() {
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var maleCheckBox: CheckBox
    private lateinit var femaleCheckBox: CheckBox
    private lateinit var apartmentsInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)
        authManager = AuthManager(this)

        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)

        firstNameInput = findViewById(R.id.firstName_input)
        lastNameInput = findViewById(R.id.lastName_input)
        maleCheckBox = findViewById(R.id.maleCheckBox)
        femaleCheckBox = findViewById(R.id.femaleCheckBox)
        apartmentsInput = findViewById(R.id.apartments_input)
        emailInput = findViewById(R.id.email_register)
        passwordInput = findViewById(R.id.createPassword_input)
        registerButton = findViewById(R.id.register_button)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registration)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerButton.setOnClickListener {
            register()
        }
    }

    private fun authenticate() {
        Toast.makeText(
            this@ActivityRegistration,
            "Вход произведен успешно",
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(this, ActivityHome::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun register() {
        val firstName = firstNameInput.text.toString()
        val lastName = lastNameInput.text.toString()
        val gender: String
        val apartments = apartmentsInput.text.toString().toIntOrNull()
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        if (maleCheckBox.isChecked && femaleCheckBox.isChecked) {
            Toast.makeText(this, "Пожалуйста, укажите либо мужской, либо женский пол.", Toast.LENGTH_SHORT).show()
            return
        } else {
            gender = when {
                maleCheckBox.isChecked -> "Male"
                femaleCheckBox.isChecked -> "Female"
                else -> ""
            }
        }
        if (!isFormCompleted(firstName, lastName, gender, apartments, email, password)) {
            Toast.makeText(this, "Пожалуйста, укажите все поля формы.", Toast.LENGTH_SHORT).show()
            return
        }
        val request = RequestRegister(firstName, lastName, gender, apartments, email, password)

        lifecycleScope.launch {
            val result = kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitClient.getApiService().registerUser(request)
                }
            }

            result.onSuccess { response ->
                if (response.isSuccessful) {
                    handleSuccessfulRegister(email, password)
                }
            }.onFailure { e ->
                Log.e("ActivityRegistration POST user register", "Error: ${e.message}", e)
            }
        }
    }

    private fun handleSuccessfulRegister(email: String, password: String) {
        userWantLogin { wantsToLogin ->
            if (wantsToLogin) {
                login(email, password)
            } else {
                Toast.makeText(
                    this@ActivityRegistration,
                    "Регистрация произведена успешно!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun userWantLogin(callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(this@ActivityRegistration)
        builder.setTitle("Войти в профиль?")
        builder.setPositiveButton("Да") { dialog, _ ->
            dialog.dismiss()
            callback(true)
        }
        builder.setNegativeButton("Нет") { dialog, _ ->
            dialog.cancel()
            callback(false)
        }

        builder.show()
    }

    private fun login(email: String, password: String) {
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

    private fun isFormCompleted(firstName: String,
                                lastName: String,
                                gender: String,
                                apartments: Int?,
                                email: String,
                                password: String): Boolean {
        return if (firstName.isEmpty() || lastName.isEmpty() || gender.isEmpty() || apartments == null || email.isEmpty() || password.isEmpty()) {
            false
        } else true
    }
}