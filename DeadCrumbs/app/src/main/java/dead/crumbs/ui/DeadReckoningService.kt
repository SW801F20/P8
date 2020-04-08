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
    private var stepCounterSensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

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
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do nothing
    }

}