package dead.crumbs.ui

import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import dead.crumbs.R
import dead.crumbs.data.MapsRepository
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class MapsViewModel (private val mapsRepository: MapsRepository) : ViewModel(){
    lateinit var map: GoogleMap

    private lateinit var sensorManager: SensorManager
    var markerList = mutableListOf<Marker>()

    var mapIsInitialized = false

    fun updateOrientation(degrees: Double){
        if(markerList.size != 0)
        {
            Log.i("MapDebug", "Setting orientation")
            Log.i("MapDebug", "Number of markers: " + markerList.size)

            moveMarker(meMarker!!,1.0,degrees)

            meMarker!!.rotation=Math.toDegrees(degrees).toFloat() + 180 //+ 180 degrees to flip the map icon to point in direction
            // note that this is a hack and actually makes it point in the opposite direction
        }
    }

    private var meMarker: Marker? = null

    fun setupMap(googleMap: GoogleMap){
        if (!mapIsInitialized)
        {
            Log.i("MapDebug", "Initializing map")
            map = googleMap
            addMarkers()
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(57.041480, 9.935950), 14f))
            map.uiSettings.isZoomControlsEnabled = true
            mapIsInitialized = true
        }
    }

    //Fills in dummy data
    private fun addMarkers()
    {
        var marker = map.addMarker(newMarker( loc = LatLng(57.041480, 9.935950), name = "Me", icon = R.mipmap.my_picture))
        markerList.add(marker)

        //Assign "Me marker" for easier update of orientation
        meMarker = marker

        val locations = arrayOf(
            LatLng(57.030972, 9.933032),
            LatLng(57.040919, 9.947623)
        )
        val titles = arrayOf("Adam", "Marie")
        val distances = arrayOf(15.0, 20.0)
        val pictures = arrayOf(R.mipmap.my_picture, R.mipmap.my_picture)

        for (i in locations.indices) {
            var marker = map.addMarker(newMarker(locations[i], titles[i], distances[i], pictures[i]))
            markerList.add(marker)
        }
    }

    private fun newMarker(loc: LatLng, name: String, distance: Double? = null, icon: Int): MarkerOptions {
        // If no distance is given, just display name
        val title: String = if (distance != null) name + " " + distance + "m" else name
        val marker = MarkerOptions()
            .position(loc)
            .title(title)
        return marker
    }

    private val METERS = 1000.0

    //Expects heading in radians
    private fun moveMarker(marker: Marker, distance: Double, heading: Double){
        val R = 6378.1 //Radius of the Earth
        val distance= 1.0 / METERS //Distance in km 0

        var lat1 = Math.toRadians(marker.position.latitude) //Current lat point converted to radians
        var lng1 = Math.toRadians(marker.position.longitude) //Current lat point converted to radians


        //Formula from
        //https://www.movable-type.co.uk/scripts/latlong.html "Destination point given distance and bearing from start point"
        var lat2 = asin( sin(lat1) * cos(distance/R) +
                cos(lat1) * sin(distance/R) * cos(heading))

        var lng2 = lng1 + atan2(
            sin(heading) * sin(distance/R) * cos(lat1),
            cos(distance/R) - sin(lat1) * sin(lat2))

        lat2 = Math.toDegrees(lat2)
        lng2 = Math.toDegrees(lng2)

        marker.position = LatLng(lat2,lng2)
    }
}