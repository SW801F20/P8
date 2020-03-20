package dead.crumbs.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import dead.crumbs.R
import dead.crumbs.utilities.InjectorUtils


//factory: RSSIViewModelFactory, viewModel: RSSIViewModel
class BluetoothActivity() : AppCompatActivity(){
    private var factory: RSSIViewModelFactory? = null
    private var viewModel: RSSIViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bluetooth_activity)
        this.factory = InjectorUtils.provideRSSIViewModelFactory()
        this.viewModel = ViewModelProviders.of(this, factory).get(RSSIViewModel::class.java)
        setupBluetooth()
    }

    private val bluetoothAdapter : BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceHardwareAddress = device?.address // MAC address

                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE) // rssi
                    val t = Toast.makeText(this@BluetoothActivity,  "Bluetooth device discovered! " + device?.address + " RSSI: " + rssi, Toast.LENGTH_LONG)
                    t. show()
                }
            }
        }
    }



    fun setupBluetooth(){

        //Get bluetooth adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            throw Exception("Device doesn't support Bluetooth")
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        //val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(receiver, filter)

    }



    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

    fun onPressDiscover(view: View){
        //Check if App has been granted necessary permission, if not request them
        checkBTPermissions()

        if (bluetoothAdapter!!.isDiscovering) {
            //Restart discovery
            bluetoothAdapter.cancelDiscovery()
            bluetoothAdapter.startDiscovery()
        }
        else{
            //Start discovery
            if(!bluetoothAdapter.startDiscovery())
                throw java.lang.Exception("Bluetooth StartDiscovery Failed")
        }
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

}