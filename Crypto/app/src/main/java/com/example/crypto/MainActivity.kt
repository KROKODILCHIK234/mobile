package com.example.crypto

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvHistory: TextView
    private val historyList = mutableListOf<SpannableString>()

    private val priceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val price = intent?.getStringExtra("price")
            val time = intent?.getStringExtra("time")
            if (price != null && time != null) {
                val fullText = "[$time] $price"
                val spannable = SpannableString(fullText)
                
                if (fullText.contains("▲")) {
                    val index = fullText.indexOf("▲")
                    spannable.setSpan(ForegroundColorSpan(Color.GREEN), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (fullText.contains("▼")) {
                    val index = fullText.indexOf("▼")
                    spannable.setSpan(ForegroundColorSpan(Color.RED), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                
                historyList.add(0, spannable)
                updateHistoryUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvHistory = findViewById(R.id.tvHistory)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (checkNotificationPermission()) {
                startCryptoService()
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopCryptoService()
        }
        
        checkNotificationPermission()
    }

    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                return false
            }
        }
        return true
    }

    private fun startCryptoService() {
        val intent = Intent(this, CryptoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        tvStatus.text = "Status: ACTIVE"
        tvStatus.setTextColor(Color.parseColor("#4CAF50"))
    }

    private fun stopCryptoService() {
        val intent = Intent(this, CryptoService::class.java)
        stopService(intent)
        tvStatus.text = "Status: STOPPED"
        tvStatus.setTextColor(Color.parseColor("#F44336"))
    }

    private fun updateHistoryUI() {
        runOnUiThread {
            tvHistory.text = ""
            for (item in historyList) {
                tvHistory.append(item)
                tvHistory.append("\n")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("com.example.crypto.PRICE_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(priceReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(priceReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(priceReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}