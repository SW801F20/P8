package dead.crumbs.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dead.crumbs.data.RSSIDist
import android.app.Service
import android.os.Binder
import android.os.IBinder
import android.provider.Settings

//factory: RSSIViewModelFactory, viewModel: RSSIViewModel
class BluetoothService() : Service(){
    var callback: ((String, Double) -> Unit)? = null

    //Called on creation of BluetoothService
    override fun onCreate() {
        super.onCreate()
        setupBluetooth()
    }

    //----------Binding--------------
    // Binder given to clients
    private val binder = LocalBinder()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }




    //----------ScanBluetooth-------------
    private val bluetoothAdapter : BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    // Create a BroadcastReceiver for ACTION_FOUND.
    private val bluetoothScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    var rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toDouble() // retrieve rssi
                    var target_mac_address: String = device!!.address         //Note Bluetooth mac address != WiFi mac address

                    //Add to RSSIViewModel through callback
                    callback?.let { it(target_mac_address, rssi) }
                }
            }
        }
    }



    private val bluetoothScanEndReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            when(action) {
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED->{
                    discover()
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
        registerReceiver(bluetoothScanReceiver, filter)

        val filter2 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(bluetoothScanEndReceiver, filter2)

        discover()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(bluetoothScanReceiver)
        unregisterReceiver(bluetoothScanEndReceiver)
    }

    private fun discover(){
        //Check if App has been granted necessary permission, if not request them

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

}