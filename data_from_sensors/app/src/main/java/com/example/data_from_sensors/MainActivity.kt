package com.example.data_from_sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.data_from_sensors.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var dataSen: ActivityMainBinding
    private lateinit var sensorManager: SensorManager

    private var lightSensor: Sensor? = null
    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private var dataSensor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataSen = DataBindingUtil.setContentView(this, R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        dataSen.radioGroup.setOnCheckedChangeListener { _, _ ->
            dataSensor = ""
            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        
        val lightReg = lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) } ?: false
        if (lightSensor == null || !lightReg) {
            Toast.makeText(this, getString(R.string.sensorAbsentL), Toast.LENGTH_SHORT).show()
        }

        val rotorReg = rotationVectorSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) } ?: false
        if (rotationVectorSensor == null || !rotorReg) {
            Toast.makeText(this, getString(R.string.sensorAbsentR), Toast.LENGTH_SHORT).show()
        }

        val accelReg = accelerometerSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) } ?: false
        if (accelerometerSensor == null || !accelReg) {
            Toast.makeText(this, getString(R.string.sensorAbsentA), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                if (dataSen.l.isChecked) {
                    dataSensor = "Освещённость: ${event.values[0]}"
                    updateUI()
                }
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                if (dataSen.r.isChecked) {
                    dataSensor = "Проекции вектора по осям:\n" +
                            "OX(${event.values[0]}),\n" +
                            "OY(${event.values[1]}),\n" +
                            "OZ(${event.values[2]})\n" +
                            "скалярная мера угла поворота: ${if (event.values.size > 3) event.values[3] else ""}"
                    updateUI()
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                if (dataSen.a.isChecked) {
                    dataSensor = "Динамическое ускорение по осям:\n" +
                            "OX(${event.values[0]}),\n" +
                            "OY(${event.values[1]}),\n" +
                            "OZ(${event.values[2]})"
                    updateUI()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateUI() {
        dataSen.sensText = dataSensor
    }
}