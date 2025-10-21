package com.example.airmonitorble

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val emailInput = findViewById<EditText>(R.id.registerEmailInput)
        val passwordInput = findViewById<EditText>(R.id.registerPasswordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val goToLoginLink = findViewById<TextView>(R.id.goToLoginLink)
        val forgotPasswordLink = findViewById<TextView>(R.id.forgotPasswordLink)

        goToLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        forgotPasswordLink.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        registerBtn.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnSuccessListener {
                        val userMap = hashMapOf(
                            "username" to username,
                            "email" to email,
                            "uid" to user.uid
                        )

                        db.collection("users").document(user.uid).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registered successfully! Verify your email before login.", Toast.LENGTH_LONG).show()
                                auth.signOut()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }?.addOnFailureListener {
                        Toast.makeText(this, "Failed to send verification email: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}