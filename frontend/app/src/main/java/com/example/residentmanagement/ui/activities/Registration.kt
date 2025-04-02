package com.example.residentmanagement.ui.activities

import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.RegisterRequest
import com.example.residentmanagement.data.network.ApiService
import javax.security.auth.callback.PasswordCallback

class Registration : AppCompatActivity() {
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val firstName = firstNameInput.text.toString()
        val lastName = lastNameInput.text.toString()
        var gender: String
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

        if (firstName.isEmpty() || lastName.isEmpty() || gender.isEmpty() || apartments == null || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, укажите все поля формы.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RegisterRequest(firstName, lastName, gender, apartments, email, password)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.userRegister(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@Registration, "Регистрация произведена успешно!", Toast.LENGTH_SHORT).show()
                    // startActivity()
                } else {
                    if (response.code() == 400) {
                        val errorBody = response.errorBody()?.string()
                        Log.e("Регистрация", "Ошибка: $errorBody")
                    } else {
                        Toast.makeText(this@Registration, "Попытка регистрации провалилась. Попробуйте еще раз.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@Registration, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}