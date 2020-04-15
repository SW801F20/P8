package dead.crumbs.ui

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import kotlin.math.min

class StepLengthEstimationService : Service(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private var mAcclerometer : Sensor? = null
    private var mStepCounter : Sensor? = null
    private var prevTimeStamp : Long? = null
    var acclerometerZs = mutableListOf<Pair<Float, Long>>()

    override fun onCreate() {
        super.onCreate()
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcclerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        mSensorManager.registerListener(this, mAcclerometer, SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mStepCounter, SENSOR_DELAY_NORMAL)
    }

    private val binder = LocalBinder()
    inner class LocalBinder : Binder(){
        fun getService() : StepLengthEstimationService = this@StepLengthEstimationService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(p0: SensorEvent?) {

        if(p0!!.sensor.type == Sensor.TYPE_STEP_COUNTER){
            val newTimeStamp = p0.timestamp
            var maxAcc : Float? = null
            var minAcc : Float? = null
            if(prevTimeStamp != null){
                for (vals in acclerometerZs){
                    if(vals.second >= prevTimeStamp!! || vals.second <= newTimeStamp){
                        if(maxAcc == null || vals.first >= maxAcc)
                            maxAcc = vals.first
                        else if(minAcc == null || vals.first <= minAcc)
                            minAcc = vals.first
                    }
                }
                acclerometerZs.clear()
                Toast.makeText(this, "step length: " + (nthRoot((maxAcc!!.toDouble() - minAcc!!.toDouble()), 4) * 0.41).toString(), Toast.LENGTH_LONG).show();
            }
            prevTimeStamp = newTimeStamp


        }

        else if(p0.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val tempPair : Pair<Float, Long> = Pair(p0.values[2], p0.timestamp)
            Log.v("Z-axis new value: ", p0.values[2].toString())
            acclerometerZs.add(tempPair)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //stopSelf()
        //mSensorManager.unregisterListener(this)
        //val max = acclerometerZs.maxBy{it.first}
        //val min = acclerometerZs.minBy{it.first}

        //Weinberg
        //Toast.makeText(this, "step length: " + (nthRoot((max!!.toDouble() - min!!.toDouble()), 4) * 0.41).toString(), Toast.LENGTH_LONG).show();

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

    fun kill(){
        onDestroy()
    }
}