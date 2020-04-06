package dead.crumbs.ui

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class StepLengthEstimationService : Service(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private var mAcclerometer : Sensor? = null
    //TODO Fake a step
    //estimate it's length

    override fun onCreate() {
        super.onCreate()
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcclerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        Log.v("y-axis change", p0.values[])

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}