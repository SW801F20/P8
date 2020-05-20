package dead.crumbs.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.view.Surface.*
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

class DeadReckoningService : Service(), SensorEventListener {
    private var accelerometer: Sensor? = null
    private lateinit var sensorManager: SensorManager
    var orientationCallback: ((Float) -> Unit)? = null
    var stepCallback: ((Double) -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        setupService()
    }

    //---ORIENTATION-START-------//
    private lateinit var rotationVectorSensor: Sensor
    private lateinit var magnetometerSensor: Sensor
    private lateinit var accelerometerSensor: Sensor

    //will be set to true later on if available
    private var useRotationVectorSensor = false

    private var rotationVector = FloatArray(5)
    private var geomagnetic = FloatArray(3)
    private var gravity = FloatArray(3)

    private var lastYawDegrees: Float = 0f

    private val ROTATION_VECTOR_SMOOTHING_FACTOR = 1f
    private val GEOMAGNETIC_SMOOTHING_FACTOR = 1f
    private val GRAVITY_SMOOTHING_FACTOR = 0.3f
    //---ORIENTATION-END-------//

    private fun setupService() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        sensorManager.registerListener(
            this,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)

    }

    //----------Binding--------------
    // Binder given to clients
    private val binder = LocalBinder()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): DeadReckoningService = this@DeadReckoningService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this)
    }

    // Step detection and step length estimation fields
    // Number of readings kept track of before wiping
    private val accArraySize: Int = 10000
    // Contains accelerometer (Z-axis) readings with value , timestamp
    private var accelerometerZReadings = arrayOfNulls<Pair<Float, Double>>(accArraySize)
    // Index of the next free slot in accelerometerZReadings
    private var accBufferIndex: Int = 0
    private var gravitationalAccel: Double = 9.80665
    // Timestamp of the first accelerometer reading collected
    private var firstTimestamp: Double = 0.0
    // Timestamp of the previous step taken
    private var previousStepTimestamp: Double = 0.0

    // This is called whenever this class (SensorEventListener) detects a new sensor value
    // from a sensor it is listening to (registerListener)
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {

            Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ACCELEROMETER -> {

                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    processAndRecordReading(event)
                    var estimatedStepLength: Double = 0.0
                    if (isStep(accelerometerZReadings)) {
                        val accelerometerValues: MutableList<Float> =
                            getAccelReadingsDuringStep(accelerometerZReadings, event)
                        estimatedStepLength = estimateStepLength(accelerometerValues)
                        accelerometerZReadings = removeOldAccelReadings(accelerometerZReadings)
                        stepCallback?.let { it(estimatedStepLength) }
                    }

                }
                //Only use mag and acc if rotation vector sensor isn't available
                if (!useRotationVectorSensor) {
                    updateYawMagAcc(event)
                }
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                // If we got here it means that rotation vector sensor is working on this device
                useRotationVectorSensor = true

                updateYawRotationVector(event)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do nothing
    }

    // -------------- Step detection and step length estimation -------------- //
    private fun processAndRecordReading(event: SensorEvent) {
        var accelReading = event.values[2]
        // If array is full (reached accArraySize), reset the array
        if (accBufferIndex > accArraySize - 1) {
            accBufferIndex = 0
            accelerometerZReadings = arrayOfNulls(accArraySize)
        }

        // High pass filter to remove influence of earth's gravity
        accelReading = highPassFilter(accelReading)

        // Save the first reading's timestamp and use to make timestamps count from 0 and up
        if (firstTimestamp == 0.0)
            firstTimestamp = event.timestamp.toDouble()
        // Add accelerometer value and timestamp to array
        accelerometerZReadings[accBufferIndex] =
            Pair(accelReading, (event.timestamp - firstTimestamp) / 1_000_000_000.0)
        accBufferIndex++

        // Low pass filter to remove high frequency noise
        accelerometerZReadings = lowPassFilter(accelerometerZReadings, accBufferIndex)
    }

    // Performs a high pass filter on an accelerometer reading
    private fun highPassFilter(accelReading: Float): Float {
        val alpha = 0.95 // Should be between 0.9 and 1.0, so we just set it in between
        gravitationalAccel =
            alpha * gravitationalAccel + (1 - alpha) * accelReading // Equation 4 in SmartPDR
        return (accelReading - gravitationalAccel).toFloat() // Equation 5 in SmartPDR
    }

    /* Perform a low pass filter to remove high frequency noise
       This function only uses the readings, not the timestamps, but extracting the readings
       and passing them to this function would require two for loops iterating over the array,
       which is bad for performance.
     */
    private fun lowPassFilter(
        accelReadings: Array<Pair<Float, Double>?>,
        arrayIndex: Int
    ): Array<Pair<Float, Double>?> {
        // Low pass filter from equation 6 in SmartPDR
        // We tried few tests with w = 3, 9, 11 and 15, where 9 and 11 seemed to be best
        val w = 11.0 // Window size
        // Filter if there are enough readings for averaging over w readings
        if (arrayIndex > w - 1) {
            // For storing the result of the summation
            var temp = 0.0F
            // Bounds for the summation
            val lower = (-(w - 1) / 2).roundToInt()
            val upper = ((w - 1) / 2).roundToInt()

            // Iterate over the w newest readings in accelReadings and calculate sum
            for (i in lower..upper)
                temp += accelReadings[arrayIndex + lower - 1 + i]!!.first
            // Perform the filtering
            val filteredAcc = ((1 / w) * temp).toFloat()
            // The middle element of the window will be overwritten with the filtered reading
            val windowMiddle = arrayIndex - ((w - 1) / 2).roundToInt() - 1
            // Use copy to make a new pair since we cannot reassign values in pair
            accelReadings[windowMiddle] = accelReadings[windowMiddle]!!.copy(first = filteredAcc)
        }
        return accelReadings
    }

    private fun isStep(accelReadings: Array<Pair<Float, Double>?>): Boolean {
        return isPeak(accelReadings)
    }

    // Implementation of equation 7a in SmartPDR
    private fun isPeak(accelReadings: Array<Pair<Float, Double>?>): Boolean {
        // Threshold for not considering small peaks as peaks. SmartPDR sets it to 0.5.
        val peakLowerThresh = 2.0
        val peakUpperThresh = 6.5

        // Defines how many accelerometer readings the current reading is compared to, in order to
        // determine if it is a peak
        val n = 10

        // Each reading is compared to its neighbouring readings to see if it is the largest in the
        // window of 'n' readings
        for (t in n / 2..accBufferIndex - n / 2) {
            var peak = true
            for (i in -n / 2..n / 2 - 1) {
                // If i == 0 current and local will be the same reading
                if (i == 0) continue
                // Check if current accel reading is larger than its n/2 neighbouring/local readings
                val current = accelReadings[t]!!.first
                val local = accelReadings[t + i]!!.first
                // Check if peak
                if (current <= local || current <= peakLowerThresh || current >= peakUpperThresh) {
                    peak = false
                    break
                }
            }
            if (peak){
                return true
            }
        }
        return false
    }

    // Extracts the accelerometer readings which occurred during the current step to be used for
    // step length estimation
    private fun getAccelReadingsDuringStep(
        accelerometerZs: Array<Pair<Float, Double>?>,
        event: SensorEvent
    ): MutableList<Float> {
        // The timestamp of the newest event (now) in seconds
        val nowSeconds: Double = (event.timestamp - firstTimestamp) / 1_000_000_000.0
        // How many seconds before the current event to look for accelerometer values if the
        // previous step was a long time ago (longer ago than lookback)
        val lookback: Double = 2.0

        // If too much time has passed since previous step
        if (nowSeconds - previousStepTimestamp > lookback) {
            // Extract accelerometer readings from 'lookback' seconds ago
            previousStepTimestamp = nowSeconds - lookback

            // previousStepTimestamp is set to be the timestamp of an actual reading, closest
            // to 'lookback' seconds ago (right after)
            for (readingPair in accelerometerZs) {
                if (readingPair!!.second > previousStepTimestamp) {
                    previousStepTimestamp = readingPair.second
                    break
                }
            }
        }

        // Find the index of the previous peak (peaks correspond to steps)
        val startIndex =
            accelerometerZs.indexOf(accelerometerZs.find { it!!.second == previousStepTimestamp })

        // Extract everything from the previous peak (step) until now
        val accelerometerValues = mutableListOf<Float>()
        for (i in startIndex..accBufferIndex - 1) {
            accelerometerValues.add(accelerometerZs[i]!!.first)
        }
        previousStepTimestamp = nowSeconds

        return accelerometerValues
    }

    private fun estimateStepLength(accelerometerValues: MutableList<Float>): Double {
        var weinbergDist = 0.0

        // Estimate step length
        if (!accelerometerValues.isEmpty()) {
            //We experienced weinberg to work the best.
            weinbergDist = weinbergEstimation(accelerometerValues)
        }
        return weinbergDist
    }

    //Used doing development for comparing with other strategies.
    private fun simpleScarletEstimation(accelerometerValues: MutableList<Float>): Double {
        // walkfudge from Jim Scarlet's code
        val k = 2.15 // This value works well for David
        val min = accelerometerValues.min()
        val max = accelerometerValues.max()
        val avg = accelerometerValues.average()

        return k * ((avg - min!!) / (max!! - min))
    }

    //Used doing development for comparing with other strategies.
    //Sourcecode: https://www.analog.com/media/en/analog-dialogue/volume-41/backburner/ped_code.zip
    private fun scarletEstimation(accelerometerValues: MutableList<Float>): Double {
        // walkfudge from Jim Scarlet's code
        val k = 0.0249
        if(accelerometerValues.isEmpty())
            return 0.0;
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

    //This function calculates the distance of a step with the Weinberg method
    private fun weinbergEstimation(accelerometerValues: MutableList<Float>): Double {
//        val k = 0.41 //Constant for scaling the step length, based on the Weinberg paper
        val k = 0.485 // This value works well for David
        val min = accelerometerValues.min()
        val max = accelerometerValues.max()

        return (nthRoot((max!!.toDouble() - min!!.toDouble()), 4) * k)
    }

    //Taken from
    //https://rosettacode.org/wiki/Nth_root#Kotlin
    private fun nthRoot(x: Double, n: Int): Double {
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

    private fun removeOldAccelReadings(accelerometerZs: Array<Pair<Float, Double>?>): Array<Pair<Float, Double>?> {
        // Remove everything in the array apart from the last reading, which is moved to the front
        accelerometerZs[0] = accelerometerZs[accBufferIndex - 1]
        for (i in 1..accBufferIndex - 1) {
            accelerometerZs[i] = null
        }
        // Reset index to point to the next available spot in the array
        accBufferIndex = 1

        return accelerometerZs
    }
    // -------------- End of step detection and step length estimation -------------- //

    private fun updateYawRotationVector(event: SensorEvent) {
        val orientation = FloatArray(3)

        // Smooth values
        rotationVector = exponentialSmoothing(
            event.values, rotationVector,
            ROTATION_VECTOR_SMOOTHING_FACTOR
        )

        // Calculate the rotation matrix
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        // Calculate the orientation
        SensorManager.getOrientation(rotationMatrix, orientation)

        val yawDegrees = getYawDegrees(orientation)
        orientationCallback?.let { it(yawDegrees) }
    }

    //Computes new yaw based on magnetometer and accelerometer
    private fun updateYawMagAcc(event: SensorEvent) {
        // Get the orientation array with Sensor.TYPE_ROTATION_VECTOR if possible (more precise), otherwise with Sensor.TYPE_MAGNETIC_FIELD and Sensor.TYPE_ACCELEROMETER combined
        val orientation = FloatArray(3)

        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = exponentialSmoothing(
                event.values, geomagnetic,
                GEOMAGNETIC_SMOOTHING_FACTOR
            )
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = exponentialSmoothing(
                event.values, gravity,
                GRAVITY_SMOOTHING_FACTOR
            )
        }
        // Calculate the rotation and inclination matrix
        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)
        // Calculate the orientation
        SensorManager.getOrientation(rotationMatrix, orientation)

        val yawDegrees = getYawDegrees(orientation)
        orientationCallback?.let { it(yawDegrees) }
    }

    private fun getYawDegrees(orientation: FloatArray): Float {
        // Calculate yaw, pitch and roll values from the orientation[] array
        // Correct values depending on the screen rotation
        val screenRotation =
            (this@DeadReckoningService.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        var yawDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
        if (screenRotation == ROTATION_0) {
            val rollDegrees = Math.toDegrees(orientation[2].toDouble()).toFloat()
            if (rollDegrees >= 90 || rollDegrees <= -90) {
                yawDegrees += 180f
            }
        } else if (screenRotation == ROTATION_90) {
            yawDegrees += 90f
        } else if (screenRotation == ROTATION_180) {
            yawDegrees += 180f
            val rollDegrees = (-Math.toDegrees(orientation[2].toDouble())).toFloat()
            if (rollDegrees >= 90 || rollDegrees <= -90) {
                yawDegrees += 180f
            }
        } else if (screenRotation == ROTATION_270) {
            yawDegrees += 270f
        }

        // Force yaw value between 0° and 360°.
        yawDegrees = (yawDegrees + 360) % 360

        lastYawDegrees = yawDegrees

        return yawDegrees
    }

    //NOTE: This might be unnecessary
    private fun exponentialSmoothing(
        newValue: FloatArray,
        lastValue: FloatArray?,
        alpha: Float
    ): FloatArray {
        val output = FloatArray(newValue.size)
        if (lastValue == null) {
            return newValue
        }
        for (i in newValue.indices) {
            output[i] = lastValue[i] + alpha * (newValue[i] - lastValue[i])
        }
        return output
    }
}
