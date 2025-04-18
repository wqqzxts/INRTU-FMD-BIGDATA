package com.example.residentmanagement.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.User
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import com.example.residentmanagement.ui.activities.ActivityMain
import com.example.residentmanagement.ui.util.CacheManager
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute.Use


class FragmentProfile : Fragment() {
    private lateinit var menuButton: ImageButton
    private lateinit var logoutButton: Button
    private lateinit var firstName: TextView
    private lateinit var lastName: TextView
    private lateinit var gender: TextView
    private lateinit var apartments: TextView
    private lateinit var apartmentsTitle: TextView
    private lateinit var email: TextView
    private lateinit var authManager: AuthManager
    private lateinit var cacheManager: CacheManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())
        cacheManager = CacheManager(requireContext())

        val isStaff = authManager.isStaff
        val cachedProfile = cacheManager.loadUserData()

        menuButton = view.findViewById(R.id.profile_menu_button)
        logoutButton = view.findViewById(R.id.button_logout)
        firstName = view.findViewById(R.id.profile_first_name)
        lastName = view.findViewById(R.id.profile_last_name)
        gender = view.findViewById(R.id.profile_gender)
        apartments = view.findViewById(R.id.profile_apartments)
        apartmentsTitle = view.findViewById(R.id.profile_apartments_title)
        apartments.visibility = if (!isStaff) View.VISIBLE else View.GONE
        apartmentsTitle.visibility = if (!isStaff) View.VISIBLE else View.GONE
        email = view.findViewById(R.id.profile_email)

        if (cachedProfile != null) {
            updateUI(cachedProfile)
        } else {
            loadProfileInfo()
        }

        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun showPopupMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.popup_menu_profile, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.profile_menu_edit -> {
                    val createFragment = FragmentProfileEdit()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.home_container, createFragment)
                        .addToBackStack("profile_fragment")
                        .commit()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun updateUI(profileData: User) {
        firstName.text = profileData.firstName
        lastName.text = profileData.lastName
        gender.text = if (profileData.gender == "Male") "Мужской" else "Женский"
        apartments.text = profileData.apartments.toString()
        email.text = profileData.email
    }

    private fun loadProfileInfo() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().getProfileInfo()

                if (response.code() == 200) {
                    val user = response.body()
                    if (user != null) {
                        val profileData = User(
                            user.firstName,
                            user.lastName,
                            user.gender,
                            user.apartments,
                            user.email,
                            isStaff = authManager.isStaff
                        )
                        cacheManager.saveUserData(profileData)

                        updateUI(profileData)
                    } else {
                        Log.e("FragmentProfile GET profile info", "Empty body in response")
                    }
                    if (response.code() == 401) {
                        loadProfileInfo()
                    }
                    if (response.code() == 403) {
                        authManager.isSessionExpiredFromApp = true
                        Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), ActivityMain::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            } catch (e: Exception) {
                Log.e("FragmentProfile GET profile info", "Error: ${e.message}")
            }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().logoutUser()

                if (response.code() == 204) {
                    authManager.clearAuthCredentials()
                    cacheManager.clearUserData()

                    val intent = Intent(requireContext(), ActivityMain::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()

                    Toast.makeText(requireContext(), "Выход произведен успешно", Toast.LENGTH_SHORT).show()
                }
                if (response.code() == 401) {
                    logout()
                }
                if (response.code() == 403) {
                    authManager.isSessionExpiredFromApp = true
                    Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), ActivityMain::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()
                }
            } catch (e: Exception) {
                Log.e("FragmentProfile POST logout", "Error: ${e.message}")
            }
        }
    }
}