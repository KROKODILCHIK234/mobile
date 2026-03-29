package com.example.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var listSensorTextView: TextView
    private lateinit var spinner: Spinner

    // Arrays of sensor types as requested
    private val environmentSensors = intArrayOf(
        Sensor.TYPE_MAGNETIC_FIELD,
        Sensor.TYPE_LIGHT,
        Sensor.TYPE_PRESSURE,
        Sensor.TYPE_RELATIVE_HUMIDITY,
        Sensor.TYPE_AMBIENT_TEMPERATURE
    )

    private val positionSensors = intArrayOf(
        Sensor.TYPE_ACCELEROMETER,
        Sensor.TYPE_GYROSCOPE,
        Sensor.TYPE_PROXIMITY,
        Sensor.TYPE_GRAVITY,
        Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_ROTATION_VECTOR,
        Sensor.TYPE_GAME_ROTATION_VECTOR,
        Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
        Sensor.TYPE_SIGNIFICANT_MOTION,
        Sensor.TYPE_STEP_DETECTOR,
        Sensor.TYPE_STEP_COUNTER,
        30 // TYPE_MOTION_DETECT
    )

    private val humanSensors = intArrayOf(21, 31, 34) // TYPE_HEART_RATE, TYPE_HEART_BEAT, TYPE_LOW_LATENCY_OFFBODY_DETECT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        listSensorTextView = findViewById(R.id.list_sensor)
        spinner = findViewById(R.id.spinner)

        // Set up the spinner with a standard layout to ensure visibility
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.type_sensors,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTypes = when (position) {
                    0 -> environmentSensors
                    1 -> positionSensors
                    2 -> humanSensors
                    else -> intArrayOf()
                }
                displaySensors(selectedTypes)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                listSensorTextView.text = ""
            }
        }
    }

    private fun displaySensors(types: IntArray) {
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val filteredSensors = allSensors.filter { sensor ->
            types.contains(sensor.type)
        }

        if (filteredSensors.isEmpty()) {
            listSensorTextView.text = ""
        } else {
            val sensorNames = filteredSensors.joinToString("\n") { it.name }
            listSensorTextView.text = sensorNames
        }
    }
}
