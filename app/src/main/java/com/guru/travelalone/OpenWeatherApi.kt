package com.guru.travelalone

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// OpenWeather API 앤드포인트 정의
interface OpenWeatherApi {
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>
}