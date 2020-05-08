package dead.crumbs.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dead.crumbs.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import dead.crumbs.utilities.InjectorUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //TODO: this should be set doing login or something
    //Hardcode the username here. Note must exist in DB with correct bluetooth mac address!
    var friends_macs = mutableListOf<String>();
    companion object{
        const val my_username = "jacob6565"
    }

    private val REQUEST_ENABLE_BT = 1
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

        //Checks permissions
        checkPermissions()

        initializeBluetoothScan()
        initializeMapsViewModel() //Must be called before "startDeadReckoning()"

        // Dead Reckoning
        startDeadReckoning()

        // Button for viewing Map (ui/MapsActivity)
        button_map.setOnClickListener{
            button_map.isClickable = false
            val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)
        }
        createLocationRequest()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()

        // Button clickable for viewing Map (ui/MapsActivity) - onResume is called when closing map
        button_map.isClickable = true
    }

    override fun onDestroy() {
        super.onDestroy()
        drService.onDestroy()
        bluetoothService.onDestroy()
    }



    //-------------Maps-----------------------//
    var mapsViewModel: MapsViewModel? = null
    fun initializeMapsViewModel(){
        val factory = InjectorUtils.singletonProvideMapsViewModelFactory()
        // Use ViewModelProviders class to create / get already created rssisViewModel
        // for this view (activity)
        mapsViewModel = ViewModelProviders.of(this, factory)
            .get(MapsViewModel::class.java)
    }



    //------------Dead Reckoning-------------//

    private lateinit var drService: DeadReckoningService

    private fun startDeadReckoning(){
        val intent = Intent(this, DeadReckoningService::class.java)
        startService(intent)
        Intent(this, DeadReckoningService::class.java).also { intent ->
            bindService(intent, connectionDeadReckoningService, Context.BIND_AUTO_CREATE)
        }
    }

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connectionDeadReckoningService = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to DeadReckoningService, cast the IBinder and get LocalService instance
            val drBinder = service as DeadReckoningService.LocalBinder
            drService = drBinder.getService()
            drService.orientationCallback = fun(yaw: Float) {       //callback function
                updateOrientation(yaw)
            }
            drService.stepCallback = fun(stepLength: Double) {       //callback function
                updatePosition(stepLength)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            // Do nothing
        }
    }

    private fun updatePosition(stepLength: Double){
        mapsViewModel!!.moveMeMarker(my_username, stepLength)
    }

    //Call the function in the viewmodel to update the orientation
    //of the meMarker.
    private fun updateOrientation(yaw : Float){
        //Parsing the yaw value on to the function in the viewmodel
        mapsViewModel!!.updateOrientation(yaw)
    }


    //------------Bluetooth Part-------------//
    var rssiViewModel : RSSIViewModel? = null

    private fun initializeBluetoothScan() {
        // Get the rssisViewModelFactory with all of it's dependencies constructed
        val factory = InjectorUtils.provideRSSIViewModelFactory()
        // Use ViewModelProviders class to create / get already created rssisViewModel
        // for this view (activity)
        rssiViewModel = ViewModelProviders.of(this, factory)
            .get(RSSIViewModel::class.java)

        friends_macs = rssiViewModel!!.getMacs()

        //Enables bluetooth
        enableBluetooth()

        //This enables Bluetooth Discoverability for 1 hour
        enableBluetoothDiscoverability()

        //Start the actual bluetooth scan
        startBluetoothScan()
    }


    private fun enableBluetoothDiscoverability() {
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600) // 3600 = 1 hour should make it not timeout
        }
        startActivity(discoverableIntent)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            var permissionCheck =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
            if (permissionCheck != 0) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 1001
                ) //Any number
            }
        }
    }

    private fun enableBluetooth(){
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            throw Exception("Device doesn't support Bluetooth")
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun startBluetoothScan(){
        val intent = Intent(this, BluetoothService::class.java)
        startService(intent)
        Intent(this, BluetoothService::class.java).also { intent ->
            bindService(intent, connectionBluetoothService, Context.BIND_AUTO_CREATE)
        }
    }


    private val rssiProximity: RSSIProximity = RSSIProximity();
    private lateinit var bluetoothService: BluetoothService
    private var boundBluetoothService: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connectionBluetoothService = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to BluetoothService, cast the IBinder and get LocalService instance
            val binder = service as BluetoothService.LocalBinder
            bluetoothService = binder.getService()
            bluetoothService.callback = fun(target_mac: String, rssi: Double) {       //callback function
                val rssi_dist = rssiProximity.distanceFromRSSI(rssi)
                val dist_threshold = 2
                //if the mac adress matches one of the users friends and distance is under threshold
                //we sync the two users and update their markers on the map.
                if (friends_macs.contains(target_mac) && rssi_dist < dist_threshold){
                    var new_locs = rssiViewModel!!.bluetoothSync(my_username, target_mac, rssi_dist);
                        for(loc in new_locs){
                            mapsViewModel?.updateLocation(loc.user_ref,
                                loc.position.coordinates!![0],
                                loc.position.coordinates[1]
                            )
                    }
                }
            }

            boundBluetoothService = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            boundBluetoothService = false
        }
    }

    //This function checks that the user has high accuracy GPS enabled and prompts them to turn it
    //on if they do not.
    fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder!!.build())
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity,
                        0x1) //this value is needed to replace REQUEST_CHECK_SETTINGS
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

}