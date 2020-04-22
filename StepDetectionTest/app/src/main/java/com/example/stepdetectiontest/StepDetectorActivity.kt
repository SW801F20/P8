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
import kotlin.math.roundToInt
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

    // For viewing data in AnyChartActivity
    var anyChartPeakTimestamps = mutableListOf<Double>()
    val anyChartAccels = mutableListOf<Float>()
    val anyChartTimestamps = mutableListOf<Double>()

    // For showing the effects of filtering
    val anyChartNotFiltered = mutableListOf<Float>()
    val anyChartHighPassFiltered = mutableListOf<Float>()
    var anyChartLowPassFiltered = mutableListOf<Float>()

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


        // Button for seeing the main chart
        button_AnyChart.setOnClickListener{
            button_AnyChart.isClickable = false
            val intent = Intent(this, LineChartActivity::class.java)

//            for ((i, readingPair) in accelerometerZs.withIndex()) {
//                if (readingPair != null) {
//                    accelReadings[i] = readingPair!!.first
//                    accelTimestamps[i] = readingPair!!.second.toDouble()
//                }
//            }

            // Data to pass to the chart
            var anyChartPeakTimestampsArray = DoubleArray(accArraySize)
            val anyChartAccelsArray = FloatArray(accArraySize)
            val anyChartTimestampsArray = DoubleArray(accArraySize)

            // Copy the data
            for (i in anyChartAccels.indices){
                    anyChartAccelsArray[i] = anyChartAccels[i]
                    anyChartTimestampsArray[i] = anyChartTimestamps[i]
            }
            for (i in anyChartPeakTimestamps.indices){
                anyChartPeakTimestampsArray[i] = anyChartPeakTimestamps[i]
            }

            // Pass the data to the chart
            intent.putExtra("ACCEL_READINGS", anyChartAccelsArray)
            intent.putExtra("ACCEL_TIMESTAMPS", anyChartTimestampsArray)
            intent.putExtra("PEAK_TIMESTAMPS", anyChartPeakTimestampsArray)

            startActivity(intent)
        }

        // Button for seeing the filter chart
        button_AnyChartFilter.setOnClickListener{
            button_AnyChartFilter.isClickable = false
            val intent = Intent(this, LineChartActivity::class.java)


            // TODO: Finish this
            // Data to pass to the chart
            val anyChartTimestampsArray = DoubleArray(accArraySize)
            val anyChartNotFilteredArray = FloatArray(accArraySize)
            val anyChartHighPassFilteredArray = FloatArray(accArraySize)
            val anyChartLowPassFiltered = FloatArray(accArraySize)
            val anyChartLowAndHighPassFilteredArray = FloatArray(accArraySize)

            // Copy the data
            for (i in anyChartAccels.indices){
                anyChartTimestampsArray[i] = anyChartTimestamps[i]
                anyChartLowAndHighPassFilteredArray[i] = anyChartAccels[i]
            }

            // Pass the data to the chart
            intent.putExtra("HIGH_AND_LOW_FILTERED_ACCS", anyChartLowAndHighPassFilteredArray)
            intent.putExtra("ACCEL_TIMESTAMPS", anyChartTimestampsArray)

            startActivity(intent)
        }

    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorValue = event.values[0]

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                processAndRecordReading(event)
//                if (accBufferIndex % 50 == 0){
                    isStep(accelerometerZs)
//                }
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

    private fun isStep(accelReadings: Array<Pair<Float, Double>?>): Boolean {

        // Threshold for not considering small peaks as peaks. SmartPDR sets it to 0.5.
        val peakLowerThresh = 2.0
        val peakUpperThresh = 6.5
        val aTauPP = 1.0
        val n = 6

        // Peak detection
        var tPeak = setOf<Float>()
        for (t in n/2 .. accBufferIndex - n/2) {
            var isPeak = true
            for (i in -n/2 .. n/2 - 1) {
//                if (i == 0 || t + i < 0 || t + i > accBufferIndex) continue
                // If i == 0 current and local will be the same reading
                if (i == 0) continue
                // Check if current accel reading is larger than its n/2 neighbouring/local readings
                val current = accelReadings[t]!!.first
                val local = accelReadings[t+i]!!.first
                // Check if not peak
                if (current <= local || current <= peakLowerThresh || current >= peakUpperThresh) {
                    isPeak = false
                    break
                }
            }
            if (isPeak) {
                tPeak.plus(accelReadings[t]!!.first)
                Log.v("Peak: ", "Peak found. Value: ${accelReadings[t]!!.first}")
                anyChartPeakTimestamps.add(accelReadings[t]!!.second)
                // Count and show a step
                sdViewModel!!.ourStepCounter++
                text_OurStepCounter.text = "OurStepCounter: ${sdViewModel!!.ourStepCounter}"

                // Copy values for viewing in AnyChartActivity before flushing
                for ((i, readingPair) in accelerometerZs.withIndex()) {
                    if (readingPair != null) {
                        anyChartAccels.add(readingPair!!.first)
                        anyChartTimestamps.add(readingPair!!.second)
                    }
                }

                //TODO: Refactor hardcode flush
                accelerometerZs = arrayOfNulls(accArraySize)
                accBufferIndex = 0
            }
        }

        var tPP = setOf<Float>()

        var tSlope = setOf<Float>()

        val tStep = tPeak
            .intersect(tPP)
            .intersect(tSlope)

        return false
    }

    private fun processAndRecordReading(event: SensorEvent) {
        var accelReading = event.values[2]
        // If array is full (reached accArraySize), reset the array
        if(accBufferIndex > accArraySize - 1) {
            accBufferIndex = 0
            accelerometerZs = arrayOfNulls(accArraySize)
        }

        // Add unfiltered value to anyChart for comparison between filter and no-filter
        anyChartNotFiltered.add(accelReading)

        // High pass filter to remove influence of earth's gravity
        accelReading = highPassFilter(accelReading)

        // Add high pass filtered value to anyChart for comparison between filter and no-filter
        anyChartHighPassFiltered.add(accelReading)

        // Save the first reading's timestamp and use to make timestamps count from 0 and up
        if (firstTimestamp == 0.0)
            firstTimestamp = event.timestamp.toDouble()
        // Add accelerometer value and timestamp to array
        accelerometerZs[accBufferIndex] = Pair(accelReading, (event.timestamp - firstTimestamp) / 1_000_000_000.0)
        accBufferIndex++

        // Log the reading in logcat
        Log.v(
            "Z-axis new value: " + ((event.timestamp - firstTimestamp)/ 1_000_000_000.0).toString()  + " ",
            (accelReading - gravitationalAccel).toString()
        )

        // Low pass filter to remove high frequency noise
        accelerometerZs = lowPassFilter(accelerometerZs, accBufferIndex)
    }

    /* Perform a low pass filter to remove high frequency noise
       This function only uses the readings, not the timestamps, but extracting the readings
       and passing them to this function would require two for loops iterating over the array,
       which is bad for performance.
     */
    private fun lowPassFilter(accelReadings: Array<Pair<Float, Double>?>, arrayIndex: Int): Array<Pair<Float, Double>?> {
        // Low pass filter from equation 6 in SmartPDR
        // We tried few tests with w = 3, 9, 11 and 15, where 9 and 11 seemed to be best
        val w = 11.0 // Window size
        // Filter if there are enough readings for averaging over w readings
        if (arrayIndex > w - 1){
            // For storing the result of the summation
            var temp = 0.0F
            // Bounds for the summation
            val lower = (-(w-1)/2).roundToInt()
            val upper = ((w-1)/2).roundToInt()

            // Iterate over the w newest readings in accelReadings and calculate sum
            for (i in lower .. upper)
                temp += accelReadings[arrayIndex + lower - 1 + i]!!.first
            // Perform the filtering
            val filteredAcc = ((1 / w) * temp).toFloat()
            // The middle element of the window will be overwritten with the filtered reading
            val windowMiddle = arrayIndex - ((w-1)/2).roundToInt() - 1
            // Use copy to make a new pair since we cannot reassign values in pair
            accelReadings[windowMiddle] = accelReadings[windowMiddle]!!.copy(first = filteredAcc)
        }
        return accelReadings
    }

    // Performs a high pass filter on an accelerometer reading
    private fun highPassFilter(accelReading: Float): Float {
        val alpha = 0.95 // Should be between 0.9 and 1.0, so we just set it in between
        gravitationalAccel = alpha * gravitationalAccel + (1 - alpha) * accelReading // Equation 4 in SmartPDR
        return (accelReading - gravitationalAccel).toFloat() // Equation 5 in SmartPDR
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
