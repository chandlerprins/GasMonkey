package com.example.airmonitorble

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class ReportProblemActivity : AppCompatActivity() {
    private lateinit var edtProblemDescription: EditText
    private lateinit var btnSubmitProblem: Button
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_problem)

        edtProblemDescription = findViewById(R.id.etProblemDescription)
        btnSubmitProblem = findViewById(R.id.btnSubmitProblem)

        btnSubmitProblem.setOnClickListener {
            submitProblem()
        }
    }

    private fun submitProblem() {
        val problemText = edtProblemDescription.text.toString().trim()

        if (problemText.isEmpty()) {
            Toast.makeText(this, "Please describe the problem", Toast.LENGTH_SHORT).show()
            return
        }

        val problemData = hashMapOf(
            "description" to problemText, "timestamp" to Date()
        )

        firestore.collection("problems").add(problemData).addOnSuccessListener {
            Toast.makeText(this, "Problem reported successfully", Toast.LENGTH_SHORT).show()
            edtProblemDescription.text.clear()
        }.addOnFailureListener { e ->
            Toast.makeText(
                this, "Failed to report problem: ${e.message}", Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
    }
}