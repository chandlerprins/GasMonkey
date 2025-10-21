package com.example.airmonitorble

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SearchDeviceActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var tvResult: TextView
    private lateinit var btnAddDevice: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_device)

        auth = FirebaseAuth.getInstance()

        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        tvResult = findViewById(R.id.tvResult)
        btnAddDevice = findViewById(R.id.btnAddDevice)

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(this, "Please enter a Device ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (query.equals("DEVICE001", ignoreCase = true)) {
                tvResult.text = "Device Found: DEVICE001"
                btnAddDevice.visibility = Button.VISIBLE
            } else {
                tvResult.text = "No device found with ID \"$query\""
                btnAddDevice.visibility = Button.GONE
            }
        }

        btnAddDevice.setOnClickListener {
            val deviceId = "DEVICE001"
            val user = auth.currentUser
            if (user != null) {
                val dbRef = FirebaseDatabase.getInstance().getReference("users/${user.uid}/device")
                dbRef.setValue(deviceId)
                    .addOnSuccessListener {
                        saveDeviceLinked()
                        Toast.makeText(this, "Device added successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to link device: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun saveDeviceLinked() {
        val prefs = getSharedPreferences("GasMonkeyPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("deviceLinked", true).apply()
    }

    companion object {
        fun shouldShowDeviceScreen(context: Context): Boolean {
            val prefs = context.getSharedPreferences("GasMonkeyPrefs", Context.MODE_PRIVATE)
            return !prefs.getBoolean("deviceLinked", false)
        }
    }
}