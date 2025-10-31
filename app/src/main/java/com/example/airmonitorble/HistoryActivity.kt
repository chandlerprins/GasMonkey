package com.example.airmonitorble

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var graph: GraphView
    private lateinit var historyRecycler: RecyclerView
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val apiUrl =
        "https://apigas20251019170918-c5fwhfgmerf2g0c7.canadaeast-01.azurewebsites.net/api/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        graph = findViewById(R.id.graph)
        historyRecycler = findViewById(R.id.historyRecycler)
        historyRecycler.layoutManager = LinearLayoutManager(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNav.selectedItemId = R.id.nav_history

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
                R.id.nav_history -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                else -> false
            }
        }

        fetchHistory()
    }

    private fun fetchHistory() {
        val retrofit = Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReadingsApi::class.java)

        scope.launch(Dispatchers.IO) {
            try {
                val readingsList = api.getAllReadings()
                    .sortedBy { parseDate(it.Timestamp)?.time ?: 0L }  // sort by parsed timestamp

                Log.d("HistoryActivity", "Fetched ${readingsList.size} readings")

                withContext(Dispatchers.Main) {
                    if (readingsList.isNotEmpty()) {
                        displayGraph(readingsList)
                        displayList(readingsList)
                    } else {
                        Toast.makeText(this@HistoryActivity, "No readings found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryActivity", "Error fetching readings", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HistoryActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun parseDate(timestamp: String): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(timestamp)
        } catch (e: Exception) {
            null
        }
    }

    private fun displayGraph(readings: List<SensorReading>) {
        val series = LineGraphSeries<DataPoint>()
        val sorted = readings.sortedBy { parseDate(it.Timestamp)?.time ?: 0L }
        for ((index, r) in sorted.withIndex()) {
            series.appendData(DataPoint(index.toDouble(), r.Aqi), true, sorted.size)
        }

        graph.removeAllSeries()
        graph.addSeries(series)
        graph.title = "AQI Over Time"
        graph.gridLabelRenderer.isHorizontalLabelsVisible = true
        graph.gridLabelRenderer.isVerticalLabelsVisible = true
    }

    private fun displayList(readings: List<SensorReading>) {
        historyRecycler.adapter = HistoryAdapter(readings.sortedByDescending { parseDate(it.Timestamp)?.time ?: 0L })
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

// Retrofit API
interface ReadingsApi {
    @GET("readings/all")
    suspend fun getAllReadings(): List<SensorReading>
}
