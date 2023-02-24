package com.example.weather

import com.google.gson.annotations.SerializedName

data class WeatherDetails(
    @SerializedName("air_temperature") val temperature: Double = 0.0,
    @SerializedName("precipitation_amount") val precipitation: Double,
    @SerializedName("wind_speed") val wind: Double = 0.0,
    @SerializedName("relative_humidity") val humidity: Double = 50.0
)

data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class WeatherResponse(
    @SerializedName("properties") val properties: WeatherProperties
)

data class WeatherProperties(
    @SerializedName("timeseries") val timeseries: List<WeatherTimeseries>
)

data class WeatherTimeseries(
    @SerializedName("data") val data: WeatherData
)

data class WeatherData(
    @SerializedName("instant") val instant: WeatherInstant
)

data class WeatherInstant(
    @SerializedName("details") val details: WeatherDetails
)
