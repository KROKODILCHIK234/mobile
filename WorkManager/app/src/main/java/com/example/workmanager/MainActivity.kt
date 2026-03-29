package com.example.workmanager

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.workmanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val weatherAdapter = WeatherAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.btnStartWork.setOnClickListener {
            startWeatherWorkChain()
        }
    }

    private fun setupRecyclerView() {
        binding.rvWeather.layoutManager = LinearLayoutManager(this)
        binding.rvWeather.adapter = weatherAdapter
    }

    private fun startWeatherWorkChain() {
        val workManager = WorkManager.getInstance(this)
        
        weatherAdapter.clear()
        binding.tvStatus.text = "Status: Starting..."
        binding.progressBar.visibility = View.VISIBLE
        binding.btnStartWork.isEnabled = false

        val cities = listOf(
            "Moscow", "Saint Petersburg", "London", 
            "New York", "Tokyo", "Paris", "Berlin", "Dubai"
        )
        
        val requests = cities.map { city ->
            OneTimeWorkRequestBuilder<WeatherWorker>()
                .addTag("weather_work")
                .setInputData(workDataOf(WeatherWorker.KEY_CITY to city))
                .build()
        }

        // Chain requests sequentially
        var continuation = workManager.beginWith(requests[0])
        for (i in 1 until requests.size) {
            continuation = continuation.then(requests[i])
        }
        
        continuation.enqueue()

        // Observe progress
        workManager.getWorkInfosByTagLiveData("weather_work").observe(this) { workInfos ->
            if (workInfos.isNullOrEmpty()) return@observe

            val finishedCount = workInfos.count { it.state.isFinished }
            binding.tvStatus.text = "Status: Updating cities ($finishedCount/${workInfos.size})"

            workInfos.forEach { workInfo ->
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val cityName = workInfo.outputData.getString(WeatherWorker.KEY_CITY_NAME)
                    val temp = workInfo.outputData.getString(WeatherWorker.KEY_TEMP)
                    val desc = workInfo.outputData.getString(WeatherWorker.KEY_DESC)
                    val icon = workInfo.outputData.getString(WeatherWorker.KEY_ICON)

                    if (cityName != null && temp != null && desc != null && icon != null) {
                        weatherAdapter.addWeather(
                            WeatherAdapter.WeatherData(cityName, temp, desc, icon)
                        )
                    }
                }
            }

            if (finishedCount == workInfos.size) {
                binding.tvStatus.text = "Status: All cities updated"
                binding.progressBar.visibility = View.GONE
                binding.btnStartWork.isEnabled = true
            }
        }
    }
}
