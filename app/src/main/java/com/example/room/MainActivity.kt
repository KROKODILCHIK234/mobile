package com.example.room

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.room.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val adapter = CompanyAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)
        val dao = db.companyDao()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            dao.getAll().collectLatest { companies ->
                if (companies.isEmpty()) {
                    seedDatabase(dao)
                }
                adapter.submitList(companies)
            }
        }

        binding.btnDelete.setOnClickListener {
            val text = binding.etSearch.text.toString()
            if (text.isNotEmpty()) {
                lifecycleScope.launch {
                    dao.deleteBySubstring(text)
                }
            }
        }

        binding.btnAnalyze.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
        }
    }

    private suspend fun seedDatabase(dao: CompanyDao) {
        val initialCompanies = listOf(
            Company(name = "Газпром", capitalization = 68012),
            Company(name = "НК «Роснефть»", capitalization = 62534),
            Company(name = "НОВАТЭК", capitalization = 51630),
            Company(name = "Норильский никель", capitalization = 50604),
            Company(name = "ЛУКОЙЛ", capitalization = 48601),
            Company(name = "Полюс", capitalization = 27738),
            Company(name = "Яндекс", capitalization = 22122),
            Company(name = "Газпром нефть", capitalization = 20406),
            Company(name = "Сургутнефтегаз", capitalization = 17405),
            Company(name = "НЛМК", capitalization = 16941),
            Company(name = "Татнефть", capitalization = 15176),
            Company(name = "Северсталь", capitalization = 15029),
            Company(name = "Полиметалл", capitalization = 11142),
            Company(name = "En+ Group", capitalization = 10000),
            Company(name = "Etalon Group", capitalization = 5000),
            Company(name = "Mail.Ru Group", capitalization = 12000),
            Company(name = "X5 Retail Group", capitalization = 18000),
            Company(name = "Sberbank", capitalization = 70000),
            Company(name = "Россети Северный Кавказ (МРСК Северного Кавказа)", capitalization = 1200)
        )
        dao.insertAll(initialCompanies)
    }
}
