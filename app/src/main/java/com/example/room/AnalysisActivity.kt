package com.example.room

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.room.databinding.ActivityAnalysisBinding
import kotlinx.coroutines.launch

class AnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalysisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getDatabase(this).companyDao()

        lifecycleScope.launch {
            val totalCap = dao.getTotalCapitalization() ?: 0
            val aboveAvg = dao.getCountAboveAverage()
            val engNames = dao.getCountEnglishNames()
            val maxCapName = dao.getHighestCapitalizationName() ?: "?"
            val longestName = dao.getLongestName() ?: "?"

            binding.tvTotalCap.text = totalCap.toString()
            binding.tvAboveAvg.text = aboveAvg.toString()
            binding.tvEngNames.text = engNames.toString()
            binding.tvMaxCapName.text = maxCapName
            binding.tvLongestName.text = longestName
        }
    }
}
