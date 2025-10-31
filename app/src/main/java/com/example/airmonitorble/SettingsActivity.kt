package com.example.airmonitorble

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnEditProfile: Button
    private lateinit var btnReportProblem: Button
    private lateinit var btnAddAccount: Button
    private lateinit var btnLogout: Button
    private lateinit var btnContactUs: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()

        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnReportProblem = findViewById(R.id.btnReportProblem)
        btnAddAccount = findViewById(R.id.btnAddAccount)
        btnLogout = findViewById(R.id.btnLogout)
        btnContactUs = findViewById(R.id.btnContactUs)

        // ---- Contact Us button ----
        btnContactUs.setOnClickListener {
            sendEmail()
        }

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnReportProblem.setOnClickListener {
            startActivity(Intent(this, ReportProblemActivity::class.java))
        }

        btnAddAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()

            val prefs = getSharedPreferences("GasMonkeyPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("isLoggedIn", false).apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNav.selectedItemId = R.id.nav_settings

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                R.id.nav_settings -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                else -> false
            }
        }
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("gasmonkeysgq@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Gas Monkey App Inquiry")
            putExtra(Intent.EXTRA_TEXT, "Hello Gas Monkey Team,\n\n") // Prefill message body
        }

        // Let user pick which email app to use
        startActivity(Intent.createChooser(emailIntent, "Send email via"))
    }
}
