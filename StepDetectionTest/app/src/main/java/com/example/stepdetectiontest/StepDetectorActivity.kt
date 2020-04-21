package com.example.stepdetectiontest

import android.content.Context
import android.content.Intent
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
    private val accArraySize : Int = 10000
    var accelerometerZs = arrayOfNulls<Pair<Float, Double>>(accArraySize)
    var accBufferIndex : Int = 0
    var gravitationalAccel : Double = 9.72
    var firstTimestamp : Double = 0.0

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
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        button_AnyChart.setOnClickListener{
            button_AnyChart.isClickable = false
            val intent = Intent(this, LineChartActivity::class.java)
            val accelReadings = FloatArray(accArraySize)
            val accelTimestamps = DoubleArray(accArraySize)

            for ((i, readingPair) in accelerometerZs.withIndex()) {
                if (readingPair != null) {
                    accelReadings[i] = readingPair!!.first
                    accelTimestamps[i] = readingPair!!.second.toDouble()
                }
            }

            intent.putExtra("ACCEL_READINGS", accelReadings)
            intent.putExtra("ACCEL_TIMESTAMPS", accelTimestamps)

            startActivity(intent)
        }

    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorValue = event.values[0]

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                var accelValue = event.values[2]

                if(accBufferIndex > accArraySize - 1) {
                    accBufferIndex = 0
                    accelerometerZs = arrayOfNulls(accArraySize)
                }

                // High pass filter to remove influence of earth's gravity
                val alpha = 0.95 // Should be between 0.9 and 1.0, so we just set it in between
                gravitationalAccel = alpha * gravitationalAccel + (1 - alpha) * accelValue // Equation 4 in SmartPDR
                accelValue = (accelValue - gravitationalAccel).toFloat() // Equation 5 in SmartPDR

                Log.v(
                    "gzt: ",  gravitationalAccel.toString()
                )

                // Low pass filter from equation 6 in SmartPDR
                val w = 3
                if (accBufferIndex > (w - 1) / 2){

                }


                // Save the first reading's timestamp and use to make timestamps count from 0 and up
                if (firstTimestamp == 0.0)
                    firstTimestamp = event.timestamp.toDouble()
                // Add accelerometer value and timestamp to array
                accelerometerZs[accBufferIndex] = Pair(accelValue, (event.timestamp - firstTimestamp) / 1_000_000_000.0)
                accBufferIndex++

                Log.v(
                    "Z-axis new value: " + ((event.timestamp - firstTimestamp)/ 1_000_000_000.0).toString()  + " ",
                    (accelValue - gravitationalAccel).toString()
                )
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
                val interval = 0.4 * 1000000000
                for (readingPair in accelerometerZs) {
                    if (readingPair == null) break
                    // Take all accel values before current step timestamp unless they are more than 'interval' old
                     // 400 ms
                    if (readingPair.second < event.timestamp && event.timestamp - readingPair.second < interval) {
                        accelerometerValues.add(readingPair.first)
                    }
                }
                if (accelerometerZs[0] != null) {

                    var simpleDist = 0.0
                    var scarletDist = 0.0
                    var weinbergDist = 0.0

                    // Estimate step length
                    if (!accelerometerValues.isEmpty()) {
                        simpleDist = simpleScarletEstimation(accelerometerValues)
                        scarletDist = scarletEstimation(accelerometerValues)
                        weinbergDist = weinbergEstimation(accelerometerValues)
                    }

                    // Update total distances
                    if (!scarletDist.isNaN()) sdViewModel!!.scarletDistSum += scarletDist
                    if (!simpleDist.isNaN()) sdViewModel!!.simpleDistSum += simpleDist
                    if (!weinbergDist.isNaN()) sdViewModel!!.weinbergDistSum += weinbergDist


                    // Update view
                    text_ScarletDist.text = "ScarletDist: $scarletDist"
                    text_ScarletDistSum.text = "ScarletDistSum: ${sdViewModel!!.scarletDistSum}"

                    text_SimpleDist.text = "SimpleDist: $simpleDist"
                    text_SimpleDistSum.text = "SimpleDistSum: ${sdViewModel!!.simpleDistSum}"

                    text_weinbergDist.text = "WeinbergDist: $weinbergDist"
                    text_weinbergDistSum.text = "WeinbergDistSum: ${sdViewModel!!.weinbergDistSum}"

                    text_AccelSize.text = "SizeOfAccel: ${accelerometerValues.size}"

                    // Clear old accel readings
                    // TODO: Make sure what we're doing makes sense
//                accelerometerZs = accelerometerZs.filter { it!!.second >= event.timestamp }.toTypedArray() // this crashes
//                    accelerometerZs = arrayOfNulls(accArraySize)
//                    accBufferIndex = 0
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

    private fun weinbergEstimation(accelerometerValues: MutableList<Float>): Double {
        val k = 0.41

        val min = accelerometerValues.min()
        val max = accelerometerValues.max()
        return (nthRoot((max!!.toDouble() - min!!.toDouble()), 4) * k)
        //Toast.makeText(this, "step length: " + (nthRoot((maxAcc.toDouble() - minAcc.toDouble()), 4) * 0.41).toString(), Toast.LENGTH_LONG).show()
        //Toast.makeText(this, "Number of steps: $stepCount", Toast.LENGTH_LONG).show()
    }

    //https://rosettacode.org/wiki/Nth_root#Kotlin
    fun nthRoot(x: Double, n: Int): Double {
        if (n < 2) throw IllegalArgumentException("n must be more than 1")
        if (x <= 0.0) throw IllegalArgumentException("x must be positive")
        val np = n - 1
        fun iter(g: Double) = (np * g + x / Math.pow(g, np.toDouble())) / n
        var g1 = x
        var g2 = iter(g1)
        while (g1 != g2) {
            g1 = iter(g1)
            g2 = iter(iter(g2))
        }
        return g1
    }
}
