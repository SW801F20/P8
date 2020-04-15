package dead.crumbs.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import dead.crumbs.R
import dead.crumbs.utilities.InjectorUtils

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener{

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initializeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MapDebug", "Destroying map")
        viewModel?.mapIsInitialized = false
        //either do this or only had markers if they
        //aren't in the list
        viewModel?.markerList = mutableListOf<Marker>()
    }

    var viewModel : MapsViewModel? = null

    private fun initializeViewModel() {
        // Get the rssisViewModelFactory with all of it's dependencies constructed
        val factory = InjectorUtils.singletonProvideMapsViewModelFactory()
        // Use ViewModelProviders class to create / get already created rssisViewModel
        // for this view (activity)
        viewModel = ViewModelProviders.of(this, factory)
            .get(MapsViewModel::class.java)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        viewModel!!.setupMap(googleMap)
        viewModel!!.map.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        p0?.showInfoWindow()
        return true
    }
}
