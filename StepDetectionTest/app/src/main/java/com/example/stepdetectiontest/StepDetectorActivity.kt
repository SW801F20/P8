package com.example.stepdetectiontest

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class StepDetectorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepDetectorSensor: Sensor? = null
    private var stepCounterSensor: Sensor? = null

    var sdViewModel : StepDetectorViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // To get this to work, follow these 2
        // add implementation "androidx.collection:collection-ktx:1.1.0" to build.gradle
        // https://stackoverflow.com/questions/48988778/cannot-inline-bytecode-built-with-jvm-target-1-8-into-bytecode-that-is-being-bui
        val model: StepDetectorViewModel by viewModels()

        sdViewModel = model
        text_StepDetector.text = "StepDetector: ${sdViewModel!!.stepDetectorCount}"
        text_StepCounter.text = "StepCounter: ${sdViewModel!!.stepCounter}"

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)


    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorValue = event.values[0]


        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR){
            sdViewModel!!.stepDetectorCount += 1
            text_StepDetector.text = "StepDetector: ${sdViewModel!!.stepDetectorCount}"
        }
        else if (event.sensor.type == Sensor.TYPE_STEP_COUNTER){
            // Set initial count value upon first reading
            if (sdViewModel!!.stepCounterInitial < 1)
                sdViewModel!!.stepCounterInitial = sensorValue.toInt()

            // #steps taken since initial value
            sdViewModel!!.stepCounter = sensorValue.toInt() - sdViewModel!!.stepCounterInitial


            text_StepCounter.text = "StepCounter: ${sdViewModel!!.stepCounter}"
            text_StepCounterTotal.text = "StepCounterTotal: $sensorValue"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Toast.makeText(this, "AccChanged to $accuracy", Toast.LENGTH_SHORT).show()
    }
}
