package dead.crumbs.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import dead.crumbs.R
import dead.crumbs.utilities.InjectorUtils


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener{

    var handler = Handler()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var viewModel : MapsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initializeViewModel()
    }

    override fun onStart() {
        super.onStart()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MapDebug", "Destroying map")
        viewModel?.mapIsInitialized = false
        //either do this or only had markers if they
        //aren't in the list
        viewModel?.markerList = mutableListOf<Marker>()
    }

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

        viewModel!!.setupMap(googleMap, this, this@MapsActivity)
        viewModel!!.map.setOnMarkerClickListener(this)

        //Runnable makes call to updateMapPositions every 5 seconds
        val context : Context = this
        val runnable = object : Runnable {
            override fun run(){
                viewModel!!.updateMapPositions(context, this@MapsActivity)
                handler.postDelayed(this, 5000)
            }
        }
        handler.postDelayed(runnable, 5000)
    }

    override fun onPause() {
        super.onPause()
        //when leaving the map, the handlers callbacks (the runnable) is removed
        handler.removeCallbacksAndMessages(null)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        p0?.showInfoWindow()
        return true
    }
}
