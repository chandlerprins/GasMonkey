package com.example.airmonitorble

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val readings: List<SensorReading>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int = readings.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val r = readings[position]

        // Parse string timestamp to Date
        val date = try {
            val parsed = inputFormat.parse(r.Timestamp)
            displayFormat.format(parsed!!)
        } catch (e: Exception) {
            r.Timestamp  // fallback if parsing fails
        }

        holder.title.text = "AQI: ${r.Aqi} | Temp: ${r.Temperature}°C"
        holder.subtitle.text =
            "Time: $date | LPG: ${r.Lpg} ppm, CO₂: ${r.Co2} ppm, NH₃: ${r.Nh3} ppm, Humidity: ${r.Humidity}%"

        holder.title.setTextColor(android.graphics.Color.WHITE)
        holder.subtitle.setTextColor(android.graphics.Color.WHITE)
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(android.R.id.text1)
        val subtitle: TextView = itemView.findViewById(android.R.id.text2)
    }
}
