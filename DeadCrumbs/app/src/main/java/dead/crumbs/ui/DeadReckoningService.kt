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
    private var accelerometer: Sensor? = null
    private lateinit var sensorManager: SensorManager
    var orientationCallback: ((FloatArray) -> Unit)? = null



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



}