package com.example.residentmanagement.ui.activities

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
import androidx.lifecycle.lifecycleScope
import com.example.residentmanagement.data.network.RetrofitClient
import kotlinx.coroutines.launch

class ActivityRegistration : AppCompatActivity() {
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var maleCheckBox: CheckBox
    private lateinit var femaleCheckBox: CheckBox
    private lateinit var apartmentsInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)

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
            try {
                val response = RetrofitClient.getApiService().registerUser(request)

                if (response.code() == 200) {
                    Toast.makeText(
                        this@ActivityRegistration,
                        "Регистрация произведена успешно!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ActivityRegistration POST register user", "Error: ${e.message}", e)
            }
        }
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