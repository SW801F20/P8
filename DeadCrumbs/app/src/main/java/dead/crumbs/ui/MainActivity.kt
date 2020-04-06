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
import android.widget.Button

import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dead.crumbs.R
import dead.crumbs.data.BluetoothRSSI
import dead.crumbs.data.RSSI
import dead.crumbs.utilities.InjectorUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeUi()
        initializeBluetoothScan()

        // Button for viewing Map (ui/MapsActivity)
        button_map.setOnClickListener{
        val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)
    }
}

    //----------Initialization of MainActivity UI----------//
    var viewModel : RSSIViewModel? = null

    private fun initializeUi() {
        // Get the rssisViewModelFactory with all of it's dependencies constructed
        val factory = InjectorUtils.provideRSSIViewModelFactory()
        // Use ViewModelProviders class to create / get already created rssisViewModel
        // for this view (activity)
        viewModel = ViewModelProviders.of(this, factory)
            .get(RSSIViewModel::class.java)

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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            var t = Toast.makeText(this@MainActivity,  "Bluetooth Enabled!", Toast.LENGTH_LONG)
            t. show()
        }
    }



    //------------Bluetooth Part-------------//
    private fun initializeBluetoothScan() {
        //Checks locations permissions, which are necessary for
        checkBTPermissions()

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

    private fun checkBTPermissions() {
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
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }


    private val rssiProximity: RSSIProximity = RSSIProximity();
    private lateinit var mService: BluetoothService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to BluetoothService, cast the IBinder and get LocalService instance
            val binder = service as BluetoothService.LocalBinder
            mService = binder.getService()
            mService.callback = fun(rssi:BluetoothRSSI) {       //callback function
                viewModel!!.addRSSI(rssi);
                printDeviceDistance(rssi, rssiProximity.getNewAverageDist(rssi));
            } 

            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    //This functions is for debugging/testing
    private fun printDeviceDistance(rssi: BluetoothRSSI, dist: Double){
        Toast.makeText(this@MainActivity, "${rssi.target_mac_address}'s distance is\n $dist m", Toast.LENGTH_LONG).show()
    }

}