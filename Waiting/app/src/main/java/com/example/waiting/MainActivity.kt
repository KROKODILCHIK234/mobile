package com.example.waiting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Вариант реализации приложения "Ждун".
 * Основная логика вынесена в отдельный класс-ресивер для чистоты кода.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private var waitCounter = 0
    private var isActive = false

    // Объявляем ресивер как внутреннее свойство
    private val jdunReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_TIME_TICK -> {
                    waitCounter++
                    statusText.text = "время созерцания: $waitCounter мин."
                }
                Intent.ACTION_BATTERY_LOW -> {
                    statusText.text = getString(R.string.low_battery)
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    statusText.text = "Ждун кушает, не мешайте..."
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    statusText.text = if (waitCounter > 0) 
                        "время созерцания: $waitCounter мин." 
                    else 
                        getString(R.string.text)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Инициализация UI
        statusText = findViewById(R.id.print)
        
        setupSystemBars()
        startMonitoring()
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startMonitoring() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(jdunReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(jdunReceiver, intentFilter)
        }
        isActive = true
    }

    // Метод для кнопки "Надоело ждать?"
    fun cancel_wait(view: View) {
        if (isActive) {
            unregisterReceiver(jdunReceiver)
            isActive = false
        }
        Toast.makeText(this, getString(R.string.toast_text), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isActive) {
            unregisterReceiver(jdunReceiver)
        }
    }
}
