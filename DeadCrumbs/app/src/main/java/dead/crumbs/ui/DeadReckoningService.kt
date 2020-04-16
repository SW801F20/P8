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

class DeadReckoningService : Service(), SensorEventListener{

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

    private var stepCounterInitial : Int = 0
    private var stepCounter : Int = 0

    /* This is called whenever this class (SensorEventListener) detects a new sensor value
     * from a sensor it is listening to (registerListener) */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER){
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
        }
        else  if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            updateOrientationAngles()
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            updateOrientationAngles()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do nothing
    }

}