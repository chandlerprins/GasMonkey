package com.example.airmonitorble

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("readings")
    suspend fun postReading(@Body reading: SensorReading): Response<Unit>

    @GET("readings/latest")
    suspend fun getLatestReading(): Response<SensorReading>
}