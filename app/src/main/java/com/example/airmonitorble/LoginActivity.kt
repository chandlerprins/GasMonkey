package com.example.airmonitorble

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.loginEmailInput)
        val passwordInput = findViewById<EditText>(R.id.loginPasswordInput)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val forgotPasswordLink = findViewById<TextView>(R.id.forgotPasswordLink)
        val goToRegisterLink = findViewById<TextView>(R.id.goToRegisterLink)

        goToRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordLink.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                auth.sendPasswordResetEmail(email).addOnSuccessListener {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                        //  Check if user has already linked a device
                        val prefs = getSharedPreferences("GasMonkeyPrefs", Context.MODE_PRIVATE)
                        val deviceLinked = prefs.getBoolean("deviceLinked", false)

                        prefs.edit().putBoolean("isLoggedIn", true).apply()

                        if (deviceLinked) {
                            // Device already linked, go straight to MainActivity
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            // First-time login, show search screen
                            startActivity(Intent(this, SearchDeviceActivity::class.java))
                        }
                        finish()
                    } else {
                        Toast.makeText(this, "Please verify your email first", Toast.LENGTH_LONG)
                            .show()
                        auth.signOut()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
