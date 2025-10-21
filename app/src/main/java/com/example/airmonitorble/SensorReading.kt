package com.example.airmonitorble

import com.google.gson.annotations.SerializedName

data class SensorReading(
    @SerializedName("timestampLong") val Timestamp: Long,
    @SerializedName("lpg_ppm") val Lpg: Double,
    @SerializedName("co2_ppm") val Co2: Double,
    @SerializedName("nh3_ppm") val Nh3: Double,
    @SerializedName("temp_c") val Temperature: Double,
    @SerializedName("humidity") val Humidity: Double,
    @SerializedName("aqi") val Aqi: Double
)
