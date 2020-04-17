package dead.crumbs.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlin.math.abs
import kotlin.math.sqrt

class DeadReckoningService : Service(), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    var orientationCallback: ((FloatArray) -> Unit)? = null

    var stepCallback: ((Double) -> Unit)? = null


    override fun onCreate() {
        super.onCreate()

        setupService()
    }

    private fun setupService() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        //Accelerometer
       var  accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometerSensor,
                                SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_UI)
        //Magnetometer
        var  magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, magneticFieldSensor,
            SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_UI)

        //Step counter
        var stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        //TODO: Consider SENSOR_DELAY_FASTEST
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)

    }


    //----------Binding--------------
    // Binder given to clients
    private val binder = LocalBinder()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    inner class LocalBinder : Binder(){
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

    //Orientation values
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        // "orientationAngles" now has up-to-date information, note that the data is in radians.

        orientationCallback?.let { it(orientationAngles) }
    }


    // Step detection fields
    private var stepCounterInitial : Int = 0
    private var stepCounter : Int = 0

    // Step length estimation fields
    private var accelerometerZReadings = mutableListOf<Pair<Long, Float>>()

    /* This is called whenever this class (SensorEventListener) detects a new sensor value
     * from a sensor it is listening to (registerListener) */
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
            Sensor.TYPE_ACCELEROMETER -> {
                // For orientation
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                updateOrientationAngles()

                // For step length estimation
                accelerometerZReadings.add(Pair(event.timestamp, event.values[2]))
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                updateOrientationAngles()
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
        accelerometerZReadings = accelerometerZReadings.filter { it.first >= stepTimestamp } as MutableList<Pair<Long, Float>>

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




}