package dead.crumbs.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dead.crumbs.R
import dead.crumbs.data.BluetoothRSSI
import dead.crumbs.utilities.InjectorUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Checks locations permissions, which are necessary for
        checkLocationPermissions()

        //initializeBluetoothScan()
        initializeMapsViewModel() //Must be called before "startDeadReckoning()"

        // Dead Reckoning
        startDeadReckoning()

        // Button for viewing Map (ui/MapsActivity)
        button_map.setOnClickListener{
            button_map.isClickable = false
            val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)
        }
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
                updatePostition(stepLength)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            // Do nothing
        }
    }

    private fun updatePostition(stepLength: Double){
        mapsViewModel!!.moveMeMarker(stepLength)
    }

    //Call the function in the viewmodel to update the orientation
    //of the meMarker.
    private fun updateOrientation(yaw : Float){
        //Parsing the yaw value on to the function in the viewmodel
        mapsViewModel!!.updateOrientation(yaw)

    }


    //------------Bluetooth Part-------------//
    var viewModel : RSSIViewModel? = null

    private fun initializeBluetoothScan() {
        // Get the rssisViewModelFactory with all of it's dependencies constructed
        val factory = InjectorUtils.provideRSSIViewModelFactory()
        // Use ViewModelProviders class to create / get already created rssisViewModel
        // for this view (activity)
        viewModel = ViewModelProviders.of(this, factory)
            .get(RSSIViewModel::class.java)

        //showBluetoothRSSIList() //include for debugging/testing of rssi

        //Enables bluetooth
        enableBluetooth()

        //This enables Bluetooth Discoverability for 1 hour
        enableBluetoothDiscoverability()

        //Start the actual bluetooth scan
        startBluetoothScan()
    }

    //Function for making list displaying measured rssi values - used for debugging/testing
    private fun showBluetoothRSSIList(){
        // Observing LiveData from the RSSIViewModel which in turn observes
        // LiveData from the repository, which observes LiveData from the DAO ☺
        viewModel!!.getRSSIs().observe(this, Observer { RSSIs ->
            val stringBuilder = StringBuilder()
            RSSIs.forEach { rssi ->
                stringBuilder.append("$rssi\n\n")
            }
            textView.text = stringBuilder.toString()
        })
    }


    private fun enableBluetoothDiscoverability() {
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600) // 3600 = 1 hour should make it not timeout
        }
        startActivity(discoverableIntent)
    }

    private fun checkLocationPermissions() {
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
            bluetoothService.callback = fun(rssi:BluetoothRSSI) {       //callback function
                viewModel!!.addRSSI(rssi);
                //printDeviceDistance(rssi, rssiProximity.getNewAverageDist(rssi)); //for debugging
            }

            boundBluetoothService = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            boundBluetoothService = false
        }
    }

    //printDeviceDistance is for debugging/testing rssi distance calculations
    private fun printDeviceDistance(rssi: BluetoothRSSI, dist: Double){
        Toast.makeText(this@MainActivity, "${rssi.target_mac_address}'s distance is\n $dist m", Toast.LENGTH_LONG).show()
    }


}