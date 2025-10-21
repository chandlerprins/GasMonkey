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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int = readings.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val r = readings[position]
        val date = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            .format(Date(r.Timestamp))
        holder.title.text = "AQI: ${r.Aqi} | Temp: ${r.Temperature}°C"
        holder.subtitle.text = "Time: $date | LPG: ${r.Lpg}, CO₂: ${r.Co2}, NH₃: ${r.Nh3}, Humidity: ${r.Humidity}%"
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(android.R.id.text1)
        val subtitle: TextView = itemView.findViewById(android.R.id.text2)
    }
}
