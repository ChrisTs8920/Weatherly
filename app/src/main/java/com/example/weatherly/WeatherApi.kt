package com.example.weatherly

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

    @Serializable
    data class HomeJsonData(
        @SerialName("coord") @Transient val coord: String = "",
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

    @Serializable
    data class CloudsData(
        @SerialName("all") val all: Int
    )

    @Serializable
    data class WeatherData(
        @SerialName("id") val id: Int,
        @SerialName("main") val main: String,
        @SerialName("description") val description: String,
        @SerialName("icon") val icon: String
    )

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

    @Serializable
    data class WindData(
        @SerialName("speed") val speed: Double,
        @SerialName("deg") val deg: Int,
        @SerialName("gust") @Transient val gust: Double = 0.0
    )

    @Serializable
    data class SysData(
        @SerialName("type") val type: Int,
        @SerialName("id") val id: Int,
        @SerialName("country") val country: String,
        @SerialName("sunrise") val sunrise: Int,
        @SerialName("sunset") val sunset: Int,
    )

    @Serializable
    data class CloudData(
        @SerialName("all") val all: Double,
    )



    @Serializable
    data class ForecastJsonData(
        @SerialName("cod") @Transient val cod: String = "",
        @SerialName("message") @Transient val message: Int = 0,
        @SerialName("cnt") @Transient val cnt: Int = 0,
        @SerialName("list") val list: List<ListData>,
        @SerialName("city") val city: CityData)

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

    @Serializable
    data class ListData(
        @SerialName("dt") val dt: Int,
        @SerialName("main") val main: MainData,
        @SerialName("weather") val weather: List<WeatherData>,
        @SerialName("clouds") val clouds: CloudData,
        @SerialName("wind") val wind: WindData,
        @SerialName("visibility") val visibility: Int,
        @SerialName("pop") val pop: Double,
        @SerialName("rain") @Transient val rain: Int = 0,
        @SerialName("snow") @Transient val snow: Int = 0,
        @SerialName("sys") @Transient val sys: Int = 0,
        @SerialName("dt_txt") val dtTxt: String
    )

    private const val apiKey = "6fc16ee5f99c053a61862db4ee092d94"

    suspend fun readMainData(city: String): HomeJsonData {
        val strUrlWeather = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey"
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
                    parseHomeJson(jsonStr)
                } else {
                    dummyData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dummyData()
            }
        }
    }

    suspend fun readForecastData(city: String): ForecastJsonData {
        val strUrlForecast = "https://api.openweathermap.org/data/2.5/forecast?q=$city&units=metric&appid=$apiKey"
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
                e.printStackTrace()
                forecastDummyData()
            }
        }
    }

    fun dummyData(): HomeJsonData { //this function provides dummy data in case of failure
        return HomeJsonData(
            coord = "",
            base = "",
            weather = listOf(WeatherData(0, "", "", "01d")),
            main = MainData(0.0, 0.0, 0.0, 0.0, 0, 0),
            visibility = 0,
            wind = WindData(10.0, 0),
            dt = 0,
            sys = SysData(0, 0, "GR", 0, 0),
            timezone = 0,
            id = 0,
            name = "City",
            cod = 0,
            clouds = CloudsData(0)
        )
    }

    fun forecastDummyData(): ForecastJsonData {
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

    private fun parseHomeJson(jsonString: String): HomeJsonData {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(jsonString)
    }

    private fun parseForecastJson(jsonString: String): ForecastJsonData {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(jsonString)
    }
}