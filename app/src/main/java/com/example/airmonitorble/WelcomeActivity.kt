package com.example.airmonitorble

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Delay to show splash screen (optional, e.g., 2 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000) // 2000 ms = 2 seconds
    }

    private fun checkLoginStatus() {
        val prefs = getSharedPreferences("GasMonkeyPrefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // User already logged in -> go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // User not logged in -> go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish() // Prevent user from returning to splash
    }
}
