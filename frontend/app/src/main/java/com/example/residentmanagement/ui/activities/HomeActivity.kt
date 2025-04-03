package com.example.residentmanagement.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.residentmanagement.R

import com.example.residentmanagement.ui.fragments.NewsFragment
import com.example.residentmanagement.ui.fragments.DocumentsFragment
import com.example.residentmanagement.ui.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView : BottomNavigationView
    private lateinit var newsFragment: NewsFragment
    private lateinit var documentsFragment: DocumentsFragment
    private lateinit var profileFragment: ProfileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        newsFragment = NewsFragment()
        documentsFragment = DocumentsFragment()
        profileFragment = ProfileFragment()

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, newsFragment)
                .commit()
            bottomNavigationView.selectedItemId = R.id.news_nav
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.news_nav -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, newsFragment).commit()
                    true
                }
                R.id.documents_nav -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, documentsFragment).commit()
                    true
                }
                R.id.profile_nav -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, profileFragment).commit()
                    true
                }
                else -> false
            }
        }
    }
}