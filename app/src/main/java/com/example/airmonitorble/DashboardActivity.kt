package com.example.airmonitorble

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var lpgText: TextView
    private lateinit var co2Text: TextView
    private lateinit var nh3Text: TextView
    private lateinit var tempText: TextView
    private lateinit var humText: TextView
    private lateinit var aqiText: TextView

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notifications disabled.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        lpgText = findViewById(R.id.lpgText)
        co2Text = findViewById(R.id.co2Text)
        nh3Text = findViewById(R.id.nh3Text)
        tempText = findViewById(R.id.tempText)
        humText = findViewById(R.id.humText)
        aqiText = findViewById(R.id.aqiText)

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Start fetching latest readings
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        scope.launch {
            while (isActive) {
                fetchLatestReading()
                delay(5000) // refresh every 5 seconds
            }
        }
    }

    private fun fetchLatestReading() {
        scope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getLatestReading()
                if (response.isSuccessful) {
                    val reading = response.body()
                    reading?.let {
                        withContext(Dispatchers.Main) {
                            updateUI(it)
                            if (it.Aqi > 150.0) sendAlert(it.Aqi)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@DashboardActivity,
                            "Failed to fetch latest reading: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(reading: SensorReading) {
        lpgText.text = "LPG: %.1f ppm".format(reading.Lpg)
        co2Text.text = "CO₂: %.1f ppm".format(reading.Co2)
        nh3Text.text = "NH₃: %.1f ppm".format(reading.Nh3)
        tempText.text = "Temp: %.1f °C".format(reading.Temperature)
        humText.text = "Humidity: %.0f %%".format(reading.Humidity)
        aqiText.text = "AQI: %.1f".format(reading.Aqi)
    }

    private fun sendAlert(aqi: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val builder = NotificationCompat.Builder(this, "air_alert")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Dangerous Air Quality!")
            .setContentText("AQI reached %.1f".format(aqi))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(this).notify(1, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "air_alert", "Air Alerts", android.app.NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
