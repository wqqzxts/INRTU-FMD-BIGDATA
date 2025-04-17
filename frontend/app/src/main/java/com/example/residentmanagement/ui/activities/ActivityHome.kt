package com.example.residentmanagement.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.residentmanagement.R

import com.example.residentmanagement.ui.fragments.FragmentNews
import com.example.residentmanagement.ui.fragments.FragmentDocuments
import com.example.residentmanagement.ui.fragments.FragmentNewsPublicationCreate
import com.example.residentmanagement.ui.fragments.FragmentProfile
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivityHome : AppCompatActivity() {
    private lateinit var bottomNavigationView : BottomNavigationView
    private lateinit var fragmentNews: FragmentNews
    private lateinit var fragmentDocuments: FragmentDocuments
    private lateinit var fragmentProfile: FragmentProfile
    private lateinit var createPublicationFragment: FragmentNewsPublicationCreate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        fragmentNews = FragmentNews()
        fragmentDocuments = FragmentDocuments()
        fragmentProfile = FragmentProfile()
        createPublicationFragment = FragmentNewsPublicationCreate()

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.home_container, fragmentNews)
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
                    supportFragmentManager.beginTransaction().replace(R.id.home_container, fragmentNews).commit()
                    true
                }
                R.id.documents_nav -> {
                    supportFragmentManager.beginTransaction().replace(R.id.home_container, fragmentDocuments).commit()
                    true
                }
                R.id.profile_nav -> {
                    supportFragmentManager.beginTransaction().replace(R.id.home_container, fragmentProfile).commit()
                    true
                }
                else -> false
            }
        }
    }
}