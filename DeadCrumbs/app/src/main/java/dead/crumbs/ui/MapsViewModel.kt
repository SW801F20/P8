package dead.crumbs.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import dead.crumbs.R
import dead.crumbs.data.MapsRepository
import dead.crumbs.ui.MainActivity
import dead.crumbs.data.RSSIDist
import java.util.*
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class MapsViewModel (private val mapsRepository: MapsRepository) : ViewModel(){
    lateinit var map: GoogleMap

    var markerList = mutableListOf<Marker>()

    var mapIsInitialized = false

    fun updateOrientation(degrees: Float){
        if(markerList.size != 0)
        {
            meMarker!!.rotation=degrees
        }
    }

    fun updateLocation(username: String, lat: Double, lng: Double){
        for(marker in markerList){
            if(marker.title == username){
                marker.position = LatLng(lat,lng)
            }
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
        var marker = map.addMarker(newMarker( loc = LatLng(57.041480, 9.935950), name = MainActivity.username, icon = R.mipmap.my_picture))
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
        if(marker.title == MainActivity.username)
            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.arrow))

        return marker
    }

    //Moves in markers current heading/direction
    fun moveMeMarker(username: String, dist: Double){
        val orientation = Math.toRadians(meMarker!!.rotation.toDouble())
        val currYear = Calendar.getInstance().get(Calendar.YEAR).toString().padStart(4,'0')
        val currMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString().padStart(2,'0')
        val currDate = Calendar.getInstance().get(Calendar.DATE).toString().padStart(2,'0')
        val currHour = Calendar.getInstance().get(Calendar.HOUR).toString().padStart(2,'0')
        val currMinute = Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2,'0')
        val currSecond = Calendar.getInstance().get(Calendar.SECOND).toString().padStart(2,'0')
        val dateTimeString = currYear + "-" + currMonth + "-" + currDate+ "T" + currHour + ":" + currMinute + ":" + currSecond

        val newLocation = mapsRepository.updateLocation(username, orientation, dist, dateTimeString)
        meMarker!!.position = LatLng(newLocation.value?.position!!.coordinates?.get(0)!!,
                                 newLocation.value?.position!!.coordinates?.get(1)!!)
    }
}
