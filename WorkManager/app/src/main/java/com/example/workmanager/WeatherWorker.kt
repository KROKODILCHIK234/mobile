package com.example.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val city = inputData.getString(KEY_CITY) ?: return Result.failure()
        
        val apiKey = BuildConfig.WEATHER_API_KEY

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(WeatherApi::class.java)

        return try {
            val response = api.getWeather(city, apiKey).execute()
            if (response.isSuccessful && response.body() != null) {
                val weather = response.body()!!
                
                val outputData = workDataOf(
                    KEY_CITY_NAME to weather.name,
                    KEY_TEMP to "${weather.main.temp.toInt()}°C",
                    KEY_DESC to weather.weather[0].description,
                    KEY_ICON to weather.weather[0].icon
                )
                Result.success(outputData)
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val KEY_CITY = "key_city"
        const val KEY_CITY_NAME = "key_city_name"
        const val KEY_TEMP = "key_temp"
        const val KEY_DESC = "key_desc"
        const val KEY_ICON = "key_icon"
    }
}
