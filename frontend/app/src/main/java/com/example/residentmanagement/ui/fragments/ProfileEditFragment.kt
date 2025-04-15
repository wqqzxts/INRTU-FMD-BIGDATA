package com.example.residentmanagement.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.RequestEditUser
import com.example.residentmanagement.data.model.User
import com.example.residentmanagement.data.network.RetrofitClient

class ProfileEditFragment : Fragment() {
    private lateinit var editButton: Button
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var genderGroup: RadioGroup
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editButton = view.findViewById(R.id.button_profile_edit)
        firstNameInput = view.findViewById(R.id.profile_edit_first_name)
        lastNameInput = view.findViewById(R.id.profile_edit_last_name)
        genderGroup = view.findViewById(R.id.profile_edit_gender_group)
        emailInput = view.findViewById(R.id.profile_edit_email)
        passwordInput = view.findViewById(R.id.profile_edit_password)
        confirmPasswordInput = view.findViewById(R.id.profile_edit_confirm_password)

        loadProfileInfo()

        editButton.setOnClickListener {
            editUser()
        }
    }

    private fun loadProfileInfo() {
        RetrofitClient.getApiService().getProfileInfo().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.code() == 200) {
                    response.body()?.let { user ->
                        firstNameInput.setText(user.firstName)
                        lastNameInput.setText(user.lastName)
                        when (user.gender) {
                            "Male" -> genderGroup.check(R.id.gender_male)
                            "Female" -> genderGroup.check(R.id.gender_female)
                        }
                        emailInput.setText(user.email)
                    }
                }
                if (response.code() == 401) {
                    loadProfileInfo()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("GET fetch user info.", "Error: ${t.message}")
            }

        })
    }

    private fun editUser() {
        val firstName = firstNameInput.text.toString()
        val lastName = lastNameInput.text.toString()
        val gender = when (genderGroup.checkedRadioButtonId) {
            R.id.gender_male -> 'M'
            R.id.gender_female -> 'F'
            else -> ' '
        }
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        if (password.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (!isPasswordConfirmed(password, confirmPassword)) {
                Toast.makeText(requireContext(), "Пароли не совпдают", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val request = if (password.isNotEmpty()) {
            RequestEditUser(firstName, lastName, gender, email, password)
        } else {
            RequestEditUser(firstName, lastName, gender, email, null)
        }

        RetrofitClient.getApiService().updateProfileInfo(request).enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    Toast.makeText(requireContext(), "Профиль был изменен успешно", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                if (response.code() == 401) {
                    editUser()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("PATCH profile", "Error: ${t.message}")
            }
        })
    }

    private fun isPasswordConfirmed(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
}