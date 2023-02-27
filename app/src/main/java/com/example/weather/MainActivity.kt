package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // ktlint-disable no-wildcard-imports
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.* // ktlint-disable no-wildcard-imports
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weather.ui.theme.WeatherTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    private val haugerud = Location(name = "Haugerud", latitude = 59.96673, longitude = 10.87069)

    private val _weatherDetails = MutableStateFlow(WeatherDetails(precipitation = 200.0))
    private val weatherDetails: StateFlow<WeatherDetails> = _weatherDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val weather by weatherDetails.collectAsState()

            WeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen(haugerud.name, weather)
                }
            }
        }
    }

    private fun createService(): WeatherService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("User-Agent", "simen.ts@hotmail.com")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.met.no/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        return retrofit.create(WeatherService::class.java)
    }

    @Composable
    fun WeatherScreen(location: String, weatherDetails: WeatherDetails) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Gray)
                .padding(start = 10.dp, top = 20.dp, end = 10.dp, bottom = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Været i dag på:\n$location",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(50.dp))
                Row {
                    val image = painterResource(if (weatherDetails.precipitation > 2.0) R.drawable.rain else R.drawable.sun)

                    Image(
                        image,
                        "",
                        modifier = Modifier.height(100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
                Row {
                    Text(text = "Temperature: ${weatherDetails.temperature}°C")
                }
                Spacer(modifier = Modifier.height(40.dp))
                Row {
                    Text(text = "Wind: ${weatherDetails.wind} m/s")
                }
                Spacer(modifier = Modifier.height(40.dp))
                Row {
                    Text(text = "Humidity: ${weatherDetails.humidity}%")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { getWeather(haugerud) }
                ) {
                    Text(text = "Update Weather")
                }
            }
        }
    }

    private fun getWeather(location: Location) = runBlocking {
        val service = createService()
        val response = service.getCurrentWeather(location.latitude, location.longitude)
        if (response.isSuccessful) {
            val newWeatherDetails = response.body()?.properties?.timeseries?.get(0)?.data?.instant?.details
            if (newWeatherDetails != null) {
                _weatherDetails.value = newWeatherDetails
            }
        } else {
            println("Error: ${response.code()} ${response.message()}")
        }
    }

    @Preview(showBackground = true, device = Devices.DEFAULT, heightDp = 720, widthDp = 360)
    @Composable
    fun DefaultPreview() {
        WeatherTheme {
            WeatherScreen("Haugerud", weatherDetails = WeatherDetails(precipitation = 1.0))
        }
    }
}
