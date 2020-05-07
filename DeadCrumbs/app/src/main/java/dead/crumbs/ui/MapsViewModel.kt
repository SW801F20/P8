package dead.crumbs.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import dead.crumbs.R
import dead.crumbs.data.MapsRepository
import io.swagger.client.models.Position
import java.util.*
import kotlin.math.*


class MapsViewModel (private val mapsRepository: MapsRepository) : ViewModel(){

    lateinit var map: GoogleMap
    var markerList = mutableListOf<Marker>() // the list of markers that are displayed on the map
    var mapIsInitialized = false
    val PERMISSION_ID = 42 //a value to check if the users gives permission to what we ask for
    val loggedInUser : String = "Jacob2" //currently the user we use as logged in user
    private var meMarker: Marker? = null //the marker corresponding to your own location

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    fun updateOrientation(degrees: Float){
        if(markerList.size != 0)
        {
            meMarker!!.rotation=degrees
        }
    }

    fun setupMap(googleMap: GoogleMap, context: Context, activity: Activity){
        if (!mapIsInitialized)
        {
            Log.i("MapDebug", "Initializing map")
            map = googleMap
            addMarkers(context, activity)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(57.041480, 9.935950), 14f))
            map.uiSettings.isZoomControlsEnabled = true
            mapIsInitialized = true
        }
    }

    //fills in markers on initialization of map
    private fun addMarkers(context: Context, activity: Activity)
    {
        //getting all the locations from the db
        try {
            val loc = getGPSLocation(context, activity)
            val users = getUsers();
            val locList : MutableList<LiveData<io.swagger.client.models.Location>> = arrayListOf()
            if(users.value != null)
                for(user in users.value!!) {
                    try {
                        locList.add(getLocation(user.username))
                    }
                    catch (e: java.lang.Exception){
                        Toast.makeText(context, e.message , Toast.LENGTH_LONG).show()
                    }
                }
            //adding the meMarker to the map
            val marker = map.addMarker(newMarker( loc = LatLng(loc!!.latitude, loc.longitude), name = "Me", icon = R.mipmap.my_picture))
            markerList.add(marker)
            //assign meMarker for easier update of orientation
            meMarker = marker

            //Making all the other markers for the map
            for (user in locList) {
                if (user.value!!.user_ref != loggedInUser) {
                    val marker = map.addMarker(
                        newMarker(
                            LatLng(
                                user.value!!.position.coordinates!![0],
                                user.value!!.position.coordinates!![1]
                            ), user.value!!.user_ref,
                            distanceInM(
                                loc.latitude, loc.longitude, user.value!!.position.coordinates!![0],
                                user.value!!.position.coordinates!![1]
                            ),
                            R.mipmap.my_picture
                        )
                    )
                    markerList.add(marker)
                }
            }
        }
        catch(e: java.lang.Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    //function for making a marker
    private fun newMarker(loc: LatLng, name: String, distance: Double? = null, icon: Int): MarkerOptions {
        // If no distance is given, just display name
        val title: String = if (distance != null) name + " " + distance + "m" else name
        val marker = MarkerOptions()
            .position(loc)
            .title(title)
        if(marker.title == "Me")
            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.arrow))

        return marker
    }

    //Moves in markers current heading/direction
    fun moveMeMarker(distance: Double){
        // This nullcheck prevents the app from crashing if a step is detected outside of the map
        if (meMarker != null)
            moveMarker(meMarker!!, distance)
    }

    //Updates the location of a marker based on the length of
    //the newest detected step and the current orientation of the marker.
    //Expects length to be in meters.
    private fun moveMarker(marker: Marker, mDist: Double){
        val heading = Math.toRadians(marker.rotation.toDouble())
        val R = 6378.1 //Radius of the Earth
        val kmDist= mDist / 1000 //Convert distance from meters to km

        val lat1 = Math.toRadians(marker.position.latitude) //Current lat point converted to radians
        val lng1 = Math.toRadians(marker.position.longitude) //Current lat point converted to radians

        //Formula from
        //https://www.movable-type.co.uk/scripts/latlong.html "Destination point given distance and bearing from start point"
        var lat2 = asin( sin(lat1) * cos(kmDist/R) +
                cos(lat1) * sin(kmDist/R) * cos(heading))

        var lng2 = lng1 + atan2(
            sin(heading) * sin(kmDist/R) * cos(lat1),
            cos(kmDist/R) - sin(lat1) * sin(lat2))

        lat2 = Math.toDegrees(lat2)
        lng2 = Math.toDegrees(lng2)

        marker.position = LatLng(lat2, lng2)
    }

    //check if user allows to access hers/his location
    private fun checkPermissions(context: Context): Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    //ask for permission
    private fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    //check if gps or network is enabled
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)// || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //check if gps or network is enabled
    private fun isNetworkEnabled(context: Context): Boolean {
        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun getGPSLocation(context: Context, activity: Activity): Location? {
        val locMan : LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var location: Location ?= null
        if (checkPermissions(context)) {
            if (isLocationEnabled(context)) {
                location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(location == null && isNetworkEnabled(context))
                    location = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                val swaggerLocation : io.swagger.client.models.Location = io.swagger.client.models.
                    Location(loggedInUser, 0.0, Position("Point", arrayOf(location.latitude, location.longitude)), getDateTime())
                postLocation(swaggerLocation)

                return location
            }
        } else {
            requestPermissions(activity)
        }
        throw Exception()
    }

    //Updates the markers on the map
    fun updateMapPositions(context: Context, activity: Activity){
        var ownLat: Double = 0.0
        var ownLong: Double = 0.0
        try {
            val users = getUsers();
            val newLocations : MutableList<LiveData<io.swagger.client.models.Location>> = arrayListOf()
            if(users.value != null) {
                for (user in users.value!!) {
                    try {
                        newLocations.add(getLocation(user.username))
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            //assign meMarker for easier update of orientation
            val newMarkerList = mutableListOf<Marker>()

            //clear map to be able to update it with new values
            map.clear()
            //for loop used to find the current user
            for (user in newLocations) {
                 if(user.value!!.user_ref == loggedInUser){
                     meMarker = map.addMarker(newMarker(LatLng(user.value!!.position.coordinates!![0],
                         user.value!!.position.coordinates!![1]), name = "Me", icon = R.mipmap.my_picture))
                     newMarkerList.add(meMarker!!)
                     ownLat = user.value!!.position.coordinates!![0]
                     ownLong = user.value!!.position.coordinates!![1]
                 }
            }

            //for loop used to add the other users to the map, map replacing the markerList
            for (user in newLocations) {
                if(user.value!!.user_ref == loggedInUser){
                   continue
                }
                else {
                    val marker = map.addMarker(
                        newMarker(
                            LatLng(
                                user.value!!.position.coordinates!![0],
                                user.value!!.position.coordinates!![1]
                            ), user.value!!.user_ref, distanceInM(ownLat, ownLong, user.value!!.position.coordinates!![0], user.value!!.position.coordinates!![1]),
                            R.mipmap.my_picture
                        )
                    )
                    marker.rotation = user.value!!.yaw.toFloat()
                    newMarkerList.add(marker)
                }
            }
            markerList = newMarkerList
        }
        catch(e: java.lang.Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    //gets the current date and time
    private fun getDateTime(): String{
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

        return dateTimeString
    }

    fun getUsers() = mapsRepository.getUsers()
    fun getUser(userName: String) = mapsRepository.getUser(userName)
    fun getLocation(userName: String) = mapsRepository.getLocation(userName)
    fun postLocation(location : io.swagger.client.models.Location) = mapsRepository.postLocation(location)

    //function for convertion degrees to Radians
    fun toRadians(degrees : Double) : Double{
        return degrees * Math.PI / 180.0
    }

    //function for getting the distance between two GPS-coordinates
    fun distanceInM(lat1 : Double, long1 : Double, lat2 : Double, long2 : Double) : Double{
        val earthRadiusM = 6371000;

        val dLat = toRadians(lat2 - lat1)
        val dLong = toRadians(long2 - long1)

        val a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLong/2) * Math.sin(dLong/2) *
                Math.cos(lat1) * Math.cos(lat2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        val rounded = Math.round((earthRadiusM * c) * 10.0)/ 10.0 // for conversion
        return rounded
    }
}