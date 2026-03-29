package com.example.workmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WeatherAdapter : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    private var weatherList = mutableListOf<WeatherData>()

    data class WeatherData(
        val city: String,
        val temp: String,
        val description: String,
        val icon: String
    )

    fun addWeather(data: WeatherData) {
        val existingIndex = weatherList.indexOfFirst { it.city == data.city }
        if (existingIndex != -1) {
            weatherList[existingIndex] = data
            notifyItemChanged(existingIndex)
        } else {
            weatherList.add(data)
            notifyItemInserted(weatherList.size - 1)
        }
    }

    fun clear() {
        weatherList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(weatherList[position])
    }

    override fun getItemCount() = weatherList.size

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCityName: TextView = itemView.findViewById(R.id.tvCityName)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvTemperature: TextView = itemView.findViewById(R.id.tvTemperature)
        private val ivWeatherIcon: ImageView = itemView.findViewById(R.id.ivWeatherIcon)

        fun bind(data: WeatherData) {
            tvCityName.text = data.city
            tvDescription.text = data.description
            tvTemperature.text = data.temp
            
            val iconUrl = "https://openweathermap.org/img/wn/${data.icon}@2x.png"
            Glide.with(itemView.context)
                .load(iconUrl)
                .into(ivWeatherIcon)
        }
    }
}
