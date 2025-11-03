package com.example.airmonitorble

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

class SessionManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("GasMonkeyPrefs", Context.MODE_PRIVATE)

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()

        prefs.edit().putBoolean("isLoggedIn", false).putBoolean("deviceLinked", false).apply()
    }
}