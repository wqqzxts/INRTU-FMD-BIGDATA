package com.example.residentmanagement.ui.fragments

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.RequestEditUser
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.ui.activities.MainActivity

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
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().getProfileInfo()

                if (response.code() == 200) {
                    val user = response.body()
                    if (user != null) {
                        firstNameInput.setText(user.firstName)
                        lastNameInput.setText(user.lastName)
                        when (user.gender) {
                            "Male" -> genderGroup.check(R.id.gender_male)
                            "Female" -> genderGroup.check(R.id.gender_female)
                        }
                        emailInput.setText(user.email)
                    } else {
                        Log.e("ProfileEditFragment GET profile info", "Empty body in response")
                    }
                    if (response.code() == 401) {
                        loadProfileInfo()
                    }
                    if (response.code() == 403) {
                        Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment GET profile info", "Error: ${e.message}")
            }
        }
    }

    private fun editUser() {
        lifecycleScope.launch {
            try {
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
                        return@launch
                    }
                }

                val request = if (password.isNotEmpty()) {
                    RequestEditUser(firstName, lastName, gender, email, password)
                } else {
                    RequestEditUser(firstName, lastName, gender, email, null)
                }

                val response = RetrofitClient.getApiService().updateProfileInfo(request)

                if (response.code() == 200) {
                    Toast.makeText(requireContext(), "Профиль был изменен успешно", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                if (response.code() == 401) {
                    loadProfileInfo()
                }
                if (response.code() == 403) {
                    Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()
                }
            } catch (e: Exception) {
                Log.e("ProfileEditFragment POST edit profile", "Error: ${e.message}")
            }
        }
    }

    private fun isPasswordConfirmed(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
}