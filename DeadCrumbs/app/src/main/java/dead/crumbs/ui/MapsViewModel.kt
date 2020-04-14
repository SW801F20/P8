package dead.crumbs.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import dead.crumbs.R
import dead.crumbs.data.MapsRepository


class MapsViewModel (private val mapsRepository: MapsRepository) : ViewModel(){
    lateinit var map: GoogleMap

    var markerList = mutableListOf<Marker>()
    var orientation: Float = 0f

    var mapIsInitialized = false

    fun updateOrientation(degrees: Float){
        if(markerList.size != 0)
        {
            Log.i("MapDebug", "Setting orientation")
            Log.i("MapDebug", "Number of markers: " + markerList.size)

            meMarker!!.rotation=degrees + 180 //+ 180 degrees to flip the map icon to point in direction
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
}