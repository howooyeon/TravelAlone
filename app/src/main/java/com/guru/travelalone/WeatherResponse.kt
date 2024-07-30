package com.guru.travelalone

// JSON 응답 매핑 데이터
data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main
)

data class Weather(
    val description: String
)

data class Main(
    val temp: Float
)