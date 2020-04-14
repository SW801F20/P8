package dead.crumbs.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dead.crumbs.R
import dead.crumbs.data.MapsRepository


class MapsViewModel (private val mapsRepository: MapsRepository) : ViewModel(){
    lateinit var map: GoogleMap

    var markerList = mutableListOf<Marker>()
    var orientation: Float = 0f

    var mapIsInitialized = false

    fun updateOrientation(orientation: Float){
        if(markerList.size != 0)
        {
            Log.i("MapDebug", "Setting orientation")
            Log.i("MapDebug", "Number of markers: " + markerList.size)
            markerList[0].rotation=orientation+180 //TODO find me a little smarter
        }
    }

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


    private fun addMarkers()
    {
        var marker = map.addMarker(newMarker( loc = LatLng(57.041480, 9.935950), name = "Me", icon = R.mipmap.my_picture))
        markerList.add(marker)

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
        //return MarkerOptions()
            //.position(loc)
            //.title(title)
            //.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, icon)))
    }
}