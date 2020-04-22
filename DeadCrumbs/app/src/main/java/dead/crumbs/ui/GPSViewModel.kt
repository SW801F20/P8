package dead.crumbs.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import dead.crumbs.data.LocationRepository

class GPSViewModel (private val locationRepository : LocationRepository) : ViewModel(){

    val PERMISSION_ID = 42;
    private lateinit var mFusedLocationClient: FusedLocationProviderClient


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
    public fun getLastLocation(context:Context, activity: Activity) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        if (checkPermissions(context)) {
            if (isLocationEnabled(context)) {
                mFusedLocationClient.lastLocation.addOnCompleteListener() { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData(activity)
                    } else {
                        Log.v( "gps", "Coordinate latitude:" + location.latitude.toString())
                        Log.v( "gps", "Coordinate longitude:" + location.longitude.toString())
                    }
                }
            } else {
            }
        } else {
            requestPermissions(activity)
        }
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


}