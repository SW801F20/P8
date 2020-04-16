package com.example.stepdetectiontest

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs
import kotlin.math.sqrt


class StepDetectorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepDetectorSensor: Sensor? = null
    private var stepCounterSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private val accArraySize : Int = 1000
    var accelerometerZs = arrayOfNulls<Pair<Float, Long>>(accArraySize)
    var accBufferIndex : Int = 0

    var sdViewModel: StepDetectorViewModel? = null

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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)


    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorValue = event.values[0]

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                Log.v(
                    "Z-axis new value: " + event.timestamp.toString() + " ",
                    event.values[2].toString()
                )
                if(accBufferIndex > accArraySize - 1)
                    accBufferIndex = 0

                accelerometerZs[accBufferIndex] = Pair(event.values[2], event.timestamp)
                accBufferIndex++
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                sdViewModel!!.stepDetectorCount += 1
                text_StepDetector.text = "StepDetector: ${sdViewModel!!.stepDetectorCount}"
            }
            Sensor.TYPE_STEP_COUNTER -> {
                // Set initial count value upon first reading
                if (sdViewModel!!.stepCounterInitial < 1)
                    sdViewModel!!.stepCounterInitial = sensorValue.toInt()

                // #steps taken since initial value
                sdViewModel!!.stepCounter = sensorValue.toInt() - sdViewModel!!.stepCounterInitial


                text_StepCounter.text = "StepCounter: ${sdViewModel!!.stepCounter}"
                text_StepCounterTotal.text = "StepCounterTotal: $sensorValue"

                val accelerometerValues = mutableListOf<Float>()

                // Extract relevant accelerometer values
                // TODO: Make sure we extract the right values
                // TODO: Research around which time in the step the STEP_COUNTER detects a step
                for (readingPair in accelerometerZs) {
                    if (readingPair == null) break
                    // Take all accel values before current step timestamp unless they are more than 'interval' old
                    val interval = 0.4 * 1000000000 // 400 ms
                    if (readingPair!!.second < event.timestamp && event.timestamp - readingPair!!.second < interval) {
                        accelerometerValues.add(readingPair!!.first)
                    }
                }
                if (accelerometerZs[0] != null) {

                    // Estimate step length using Scarlet approach
                    val simpleDist = simpleScarletEstimation(accelerometerValues)
                    val scarletDist = scarletEstimation(accelerometerValues)

                    // Update total distances
                    if (!scarletDist.isNaN()) sdViewModel!!.scarletDistSum += scarletDist
                    if (!simpleDist.isNaN()) sdViewModel!!.simpleDistSum += simpleDist


                    // Update view
                    text_ScarletDist.text = "ScarletDist: $scarletDist"
                    text_ScarletDistSum.text = "ScarletDistSum: ${sdViewModel!!.scarletDistSum}"

                    text_SimpleDist.text = "SimpleDist: $simpleDist"
                    text_SimpleDistSum.text = "SimpleDistSum: ${sdViewModel!!.simpleDistSum}"

                    text_AccelSize.text = "SizeOfAccel: ${accelerometerValues.size}"

                    // Clear old accel readings
                    // TODO: Make sure what we're doing makes sense
//                accelerometerZs = accelerometerZs.filter { it!!.second >= event.timestamp }.toTypedArray()
                    accelerometerZs = arrayOfNulls(accArraySize)
                    accBufferIndex = 0
                }
            }
        }
    }

    // TODO: Test if works
    private fun scarletEstimation(accelerometerValues: MutableList<Float>): Double {
        // walkfudge from J. Scarlet's code
        val k = 0.0249
        // Constant from Indonesian paper
        // https://www.researchgate.net/publication/261381305_Smartphone-based_Pedestrian_Dead_Reckoning_as_an_Indoor_Positioning_System
//        val k = 0.81

        val min = accelerometerValues.min()
        val max = accelerometerValues.max()
        val avg = accelerometerValues.average().toFloat()

        var velocity = 0.0F
        var displace = 0.0F

        // Calculate the double summation and place result in 'displace'
        for (value in accelerometerValues) {
            velocity += value - avg
            displace += velocity
        }

        return k * sqrt(abs(((max!! - min!!) / (avg - min)) * displace))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Toast.makeText(this, "AccChanged to $accuracy", Toast.LENGTH_SHORT).show()
        // TODO: Fix app crash at screen turn to horizontal
    }

    // TODO: Doesn't provide accurate distances at the moment
    private fun simpleScarletEstimation(accelerometerValues: MutableList<Float>): Double {
        // walkfudge from Jim Scarlet's code
        val k = 0.0249

        val min = accelerometerValues.min()
        val max = accelerometerValues.max()
        val avg = accelerometerValues.average()

        return k * ((avg - min!!) / (max!! - min))
    }
}
