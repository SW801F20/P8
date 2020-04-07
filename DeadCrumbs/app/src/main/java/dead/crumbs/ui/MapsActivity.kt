package dead.crumbs.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dead.crumbs.R
import dead.crumbs.utilities.InjectorUtils
import kotlinx.android.synthetic.main.activity_main.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener{

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    var viewModel : MapsViewModel? = null

    private fun initializeUi() {
        // Get the rssisViewModelFactory with all of it's dependencies constructed
        val factory = InjectorUtils.provideMapsViewModelFactory()
        // Use ViewModelProviders class to create / get already created rssisViewModel
        // for this view (activity)
        viewModel = ViewModelProviders.of(this, factory)
            .get(MapsViewModel::class.java)

        // Observing LiveData from the RSSIViewModel which in turn observes
        // LiveData from the repository, which observes LiveData from the DAO ☺
        /*viewModel!!.getRSSIs().observe(this, Observer { RSSIs -> //TODO liveupdate here
            val stringBuilder = StringBuilder()
            RSSIs.forEach { rssi ->
                stringBuilder.append("$rssi\n\n")
            }
            textView.text = stringBuilder.toString()
        })*/
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
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
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        p0?.showInfoWindow()
        return true
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
