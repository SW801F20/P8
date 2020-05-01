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
import kotlin.math.sqrt

class DeadReckoningService : Service(), SensorEventListener{
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

        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST)

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)

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


    // Step detection fields
    private var stepCounterInitial: Int = 0
    private var stepCounter: Int = 0

    // Step length estimation fields
    private var accelerometerZReadings = mutableListOf<Pair<Long, Float>>()

    // This is called whenever this class (SensorEventListener) detects a new sensor value
    // from a sensor it is listening to (registerListener)
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val sensorValue = event.values[0]
                // Set initial count value upon first reading
                if (stepCounterInitial < 1)
                    stepCounterInitial = sensorValue.toInt()
                // Update counter with #steps taken since initial value
                stepCounter = sensorValue.toInt() - stepCounterInitial

                //TODO: Find out how and where to pass value on to
                // See LogCat in Android Studio, make sure Verbose is selected
                // and then search for stepCounter
                Log.d("stepCounter: ", stepCounter.toString())

                //TODO: Find out how and where to pass value on to
                // Step length estimation
                val stepLength = estimateStepLength(event.timestamp)
                // See LogCat in Android Studio, make sure Verbose is selected
                // and then search for stepLength
                Log.d("stepLength: ", stepLength.toString())
                stepCallback?.let { it(stepLength) }
            }
            Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ACCELEROMETER -> {
                // For step length estimation
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    accelerometerZReadings.add(Pair(event.timestamp, event.values[2]))
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

    private fun estimateStepLength(stepTimestamp: Long): Double {
        val accelerometerValues = mutableListOf<Float>()
        // Extract relevant accelerometer values
        // TODO: Make sure we extract the right values
        // TODO: Research around which time in the step the STEP_COUNTER detects a step
        for (value in accelerometerZReadings) {
            // Take all accel values before current step timestamp unless they are more than a second old
            if (value.first < stepTimestamp && stepTimestamp - value.first < 1000000000) {
                accelerometerValues.add(value.second)
            }
        }

        // Estimate step length using Scarlet approach
        val simpleDist = simpleScarletEstimation(accelerometerValues)
        val scarletDist = scarletEstimation(accelerometerValues)

        // Clear old accelerometer readings
        // TODO: Make sure we're removing the right values
        accelerometerZReadings =
            accelerometerZReadings.filter { it.first >= stepTimestamp } as MutableList<Pair<Long, Float>>

        return scarletDist
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

    // TODO: Doesn't provide accurate distances at the moment
    private fun scarletEstimation(accelerometerValues: MutableList<Float>): Double {
        // walkfudge from Jim Scarlet's code
        val k = 0.0249

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
        val k = 0.41 //Constant for scaling the step length, based on the Weinberg paper
        val min = accelerometerValues.min()
        val max = accelerometerValues.max()

        return (nthRoot((max!!.toDouble() - min!!.toDouble()), 4) * k)
    }

    //Taken from
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