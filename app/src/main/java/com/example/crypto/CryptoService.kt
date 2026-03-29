package com.example.crypto

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CryptoService : Service() {

    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private val CHANNEL_ID = "CryptoServiceChannel"
    private val NOTIFICATION_ID = 1
    private val API_KEY = "b140378781fe056078180a4235332df5d2960cbe95aa363f1552d3e47a1751f8"
    private val SYMBOL = "BTC"
    
    private var lastPrice: Double? = null
    private val threshold = 0.01 // Минимальное изменение для уведомления

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Starting tracking...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startTracking()
        return START_STICKY
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Crypto Monitor")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(price: Double, arrow: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val content = "$SYMBOL: $price USD $arrow"
        notificationManager.notify(NOTIFICATION_ID, createNotification(content))
    }

    private fun startTracking() {
        if (runnable != null) return
        runnable = object : Runnable {
            override fun run() {
                fetchPrice()
                handler.postDelayed(this, 10000)
            }
        }
        handler.post(runnable!!)
    }

    private fun fetchPrice() {
        val url = "https://min-api.cryptocompare.com/data/price?fsym=$SYMBOL&tsyms=USD&api_key=$API_KEY"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CryptoService", "Fetch failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    try {
                        val json = JSONObject(body)
                        val currentPrice = json.getDouble("USD")
                        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                        var arrow = ""
                        lastPrice?.let { last ->
                            if (currentPrice > last) arrow = "▲"
                            else if (currentPrice < last) arrow = "▼"
                        }
                        
                        // Обновляем уведомление в шторке
                        updateNotification(currentPrice, arrow)

                        // Отправляем данные в Activity
                        val intent = Intent("com.example.crypto.PRICE_UPDATE")
                        intent.setPackage(packageName)
                        intent.putExtra("price", "$SYMBOL: $currentPrice USD $arrow")
                        intent.putExtra("time", time)
                        sendBroadcast(intent)

                        lastPrice = currentPrice
                    } catch (e: Exception) {
                        Log.e("CryptoService", "Parse error", e)
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        runnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Crypto tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}