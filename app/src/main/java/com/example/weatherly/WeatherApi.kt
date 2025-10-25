package com.example.weatherly

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import kotlin.collections.List
import java.net.URL

object WeatherApi {

    // Data for Main screen
    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class HomeJsonData(
        @SerialName("coord") val coord: Coords,
        @SerialName("weather") val weather: List<WeatherData>,
        @SerialName("base") val base: String,
        @SerialName("main") val main: MainData,
        @SerialName("visibility") val visibility: Int,
        @SerialName("wind") val wind: WindData,
        @SerialName("rain") @Transient val rain: Int = 0,
        @SerialName("snow") @Transient val snow: Int = 0,
        @SerialName("clouds") val clouds: CloudsData,
        @SerialName("dt") val dt: Int,
        @SerialName("sys") val sys: SysData,
        @SerialName("timezone") val timezone: Int,
        @SerialName("id") val id: Int,
        @SerialName("name") val name: String,
        @SerialName("cod") val cod: Int,
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class Coords(
        @SerialName("lon") val lon: Double,
        @SerialName("lat") val lat: Double
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class CloudsData(
        @SerialName("all") val all: Int
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class WeatherData(
        @SerialName("id") val id: Int,
        @SerialName("main") val main: String,
        @SerialName("description") val description: String,
        @SerialName("icon") val icon: String
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class MainData(
        @SerialName("temp") val temp: Double,
        @SerialName("feels_like") val feelsLike: Double,
        @SerialName("temp_min") val tempMin: Double,
        @SerialName("temp_max") val tempMax: Double,
        @SerialName("pressure") val pressure: Int,
        @SerialName("humidity") val humidity: Int,
        @SerialName("sea_level") val seaLevel: Int? = 0,
        @SerialName("grnd_level") val grndLevel: Int? = 0,
        @SerialName("temp_kf") @Transient val tempKf: Int = 0
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class WindData(
        @SerialName("speed") val speed: Double,
        @SerialName("deg") val deg: Int,
        @SerialName("gust") @Transient val gust: Double = 0.0
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class SysData(
        @SerialName("type") val type: Int,
        @SerialName("id") val id: Int,
        @SerialName("country") val country: String,
        @SerialName("sunrise") val sunrise: Int,
        @SerialName("sunset") val sunset: Int,
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class CloudData(
        @SerialName("all") val all: Double,
    )


    // Data for Forecast Screen
    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class ForecastJsonData(
        @SerialName("cod") @Transient val cod: String = "",
        @SerialName("message") @Transient val message: Int = 0,
        @SerialName("cnt") @Transient val cnt: Int = 0,
        @SerialName("list") val list: List<ListData>,
        @SerialName("city") val city: CityData)

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class CityData(
        @SerialName("id") val id: Int,
        @SerialName("name") val name: String,
        @SerialName("coord") @Transient val coord: String = "",
        @SerialName("country") val country: String,
        @SerialName("population") @Transient val population: Int = 0,
        @SerialName("timezone") val timezone: Int,
        @SerialName("sunrise") val sunrise: Int,
        @SerialName("sunset") val sunset: Int,
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class ListData(
        @SerialName("dt") val dt: Int,
        @SerialName("main") val main: MainData,
        @SerialName("weather") val weather: List<WeatherData>,
        @SerialName("clouds") val clouds: CloudData,
        @SerialName("wind") val wind: WindData,
        @SerialName("visibility") val visibility: Int,
        @SuppressLint("UnsafeOptInUsageError") @SerialName("pop") val pop: Double,
        @SerialName("rain") @Transient val rain: Int = 0,
        @SerialName("snow") @Transient val snow: Int = 0,
        @SerialName("sys") @Transient val sys: Int = 0,
        @SerialName("dt_txt") val dtTxt: String
    )

    // Data for Air quality Index - Main screen
    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class AQI(
        @SerialName("coord") @Transient val coord: Int = 0,
        @SerialName("list") val list: List<AQIList>
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class AQIList(
        @SerialName("dt") @Transient val dt: Int = 0,
        @SerialName("main") val main: AQIMain,
        @SerialName("components") val components: AQIComponents
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class AQIMain(
        @SerialName("aqi") val aqi: Int
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class AQIComponents(
        @SerialName("co") val co: Double,
        @SerialName("no") val no: Double,
        @SerialName("no2") val no2: Double,
        @SerialName("o3") val o3: Double,
        @SerialName("so2") val so2: Double,
        @SerialName("pm2_5") val pm2_5: Double,
        @SerialName("pm10") val pm10: Double,
        @SerialName("nh3") val nh3: Double
    )

    private const val APIKEY = "6fc16ee5f99c053a61862db4ee092d94"
    private var currLon = 0.0
    private var currLat = 0.0

    suspend fun readMainData(city: String): HomeJsonData {
        val strUrlWeather = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$APIKEY"
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(strUrlWeather)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonStr = response.toString()
                    currLon = parseHomeJson(jsonStr).coord.lon
                    currLat = parseHomeJson(jsonStr).coord.lat
                    parseHomeJson(jsonStr)
                } else {
                    dummyData()
                }
            } catch (e: Exception) {
                Log.e("WeatherApi", "Error fetching weather data: ${e.message}")
                dummyData()
            }
        }
    }

    suspend fun readForecastData(city: String): ForecastJsonData {
        val strUrlForecast = "https://api.openweathermap.org/data/2.5/forecast?q=$city&units=metric&appid=$APIKEY"
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(strUrlForecast)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonStr = response.toString()
                    parseForecastJson(jsonStr)
                } else {
                    forecastDummyData()
                }
            } catch (e: Exception) {
                Log.e("WeatherApi", "Error fetching forecast data: ${e.message}")
                forecastDummyData()
            }
        }
    }

    suspend fun readAQIData(): AQI {
        val strUrlForecast = "https://api.openweathermap.org/data/2.5/air_pollution?lat=$currLat&lon=$currLon&appid=$APIKEY"
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(strUrlForecast)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonStr = response.toString()
                    parseAQIJson(jsonStr)
                } else {
                    dummyDataAQI()
                }
            } catch (e: Exception) {
                Log.e("WeatherApi", "Error fetching AQI data: ${e.message}")
                dummyDataAQI()
            }
        }
    }

    fun dummyData(): HomeJsonData { // placeholder data while app is loading
        return HomeJsonData(
            coord = Coords(0.0, 0.0),
            base = "",
            weather = listOf(WeatherData(0, "", "", "01d")),
            main = MainData(0.0, 0.0, 0.0, 0.0, 0, 0),
            visibility = 0,
            wind = WindData(0.0, 0),
            dt = 0,
            sys = SysData(0, 0, "", 0, 0),
            timezone = 0,
            id = 0,
            name = "",
            cod = 0,
            clouds = CloudsData(0)
        )
    }

    fun forecastDummyData(): ForecastJsonData { // placeholder data while app is loading
        val temp = List(40) {ListData(0,
            MainData(0.0, 0.0, 0.0, 0.0, 0, 0),
            listOf(WeatherData(0, "", "", "01d")),
            CloudData(0.0),
            WindData(0.0, 0),
            0,
            0.0,
            0,
            0,
            0,
            "")}.toMutableList()
        for (i in 0..< temp.size) {
            temp[i] = (ListData(0,
                MainData(0.0, 0.0, 0.0, 0.0, 0, 0),
                listOf(WeatherData(0, "", "", "01d")),
                CloudData(0.0),
                WindData(0.0, 0),
                0,
                0.0,
                0,
                0,
                0,
                ""))
        }
        return ForecastJsonData(
            list = temp,
            city = CityData(0, "", "", "", 0, 0, 0, 0)
        )
    }

    fun dummyDataAQI(): AQI {
        return AQI(
            list = listOf(AQIList(main = AQIMain(0), components = AQIComponents(
                co = 0.0,
                no = 0.0,
                no2 = 0.0,
                o3 = 0.0,
                so2 = 0.0,
                pm2_5 = 0.0,
                pm10 = 0.0,
                nh3 = 0.0
            )))
        )
    }

    private fun parseHomeJson(jsonString: String): HomeJsonData {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(jsonString)
    }

    private fun parseForecastJson(jsonString: String): ForecastJsonData {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(jsonString)
    }

    private fun parseAQIJson(jsonString: String): AQI {
        val json = Json {ignoreUnknownKeys = true }
        return json.decodeFromString(jsonString)
    }
}