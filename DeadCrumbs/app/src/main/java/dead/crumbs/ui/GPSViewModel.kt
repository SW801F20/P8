package dead.crumbs.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import dead.crumbs.data.LocationRepository
import io.swagger.client.models.Position
import okhttp3.internal.wait
//import java.time.LocalDateTime

import java.util.Calendar

class GPSViewModel (private val locationRepository : LocationRepository) : ViewModel(){
/*
    val PERMISSION_ID = 42;
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var callback:(() -> Location)? = null

    private fun checkPermissions(context: Context): Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    private fun isLocationEnabled(context: Context): Boolean {
        var locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    @SuppressLint("MissingPermission")
    public fun getLastLocation(context:Context, activity: Activity): Location? {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        var locMan : LocationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        var location: Location ?= null
        if (checkPermissions(context)) {
            if (isLocationEnabled(context)) {
                location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(location == null)
                    location = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                Log.v("gps", "Coordinate latitude:" + location!!.latitude.toString())
                Log.v("gps", "Coordinate longitude:" + location!!.longitude.toString())

                return location
                /*mFusedLocationClient.lastLocation.addOnCompleteListener() { task ->
                    location = task.result
                    if (location == null) {
                        requestNewLocationData(activity)
                    } else {

                        val currYear =
                            Calendar.getInstance().get(Calendar.YEAR).toString().padStart(4, '0')
                        val currMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString()
                            .padStart(2, '0')
                        val currDate =
                            Calendar.getInstance().get(Calendar.DATE).toString().padStart(2, '0')
                        val currHour =
                            Calendar.getInstance().get(Calendar.HOUR).toString().padStart(2, '0')
                        val currMinute =
                            Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2, '0')
                        val currSecond =
                            Calendar.getInstance().get(Calendar.SECOND).toString().padStart(2, '0')
                        val dateTimeString =
                            currYear + "-" + currMonth + "-" + currDate + "T" + currHour + ":" + currMinute + ":" + currSecond

                        Log.v("gps", "Coordinate latitude:" + location!!.latitude.toString())
                        Log.v("gps", "Coordinate longitude:" + location!!.longitude.toString())
                    }
                }*/


            } else { }
        } else {
            requestPermissions(activity)
        }
        throw Exception()
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(activity: Activity) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation

        }
    }

    fun getUsers() = locationRepository.getUsers()
    fun getUser(userName: String) = locationRepository.getUser(userName)
    fun getLocation(userName: String) = locationRepository.getLocation(userName)
    fun postLocation(location : io.swagger.client.models.Location) = locationRepository.postLocation(location)

*/
}