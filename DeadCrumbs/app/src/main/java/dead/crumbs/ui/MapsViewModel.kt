package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dead.crumbs.R
import dead.crumbs.data.MapsRepository


class MapsViewModel (private val mapsRepository: MapsRepository) : ViewModel(){
    lateinit var map: GoogleMap

    fun setupMap(googleMap: GoogleMap){
        map = googleMap

        map.addMarker(newMarker( loc = LatLng(57.041480, 9.935950), name = "Me", icon = R.mipmap.my_picture))

        val locations = arrayOf(
            LatLng(57.030972, 9.933032),
            LatLng(57.040919, 9.947623)
        )
        val titles = arrayOf("Adam", "Marie")
        val distances = arrayOf(15.0, 20.0)
        val pictures = arrayOf(R.mipmap.my_picture, R.mipmap.my_picture)

        for (i in locations.indices) {
            map.addMarker(newMarker(locations[i], titles[i], distances[i], pictures[i]))
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(57.041480, 9.935950), 14f))
        map.uiSettings.isZoomControlsEnabled = true

    }

    private fun newMarker(loc: LatLng, name: String, distance: Double? = null, icon: Int): MarkerOptions {
        // If no distance is given, just display name
        val title: String = if (distance != null) name + " " + distance + "m" else name
        return MarkerOptions()
            .position(loc)
            .title(title)
            .rotation(90f)
        //.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, icon)))
    }
}