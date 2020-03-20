package dead.crumbs.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        initializeUi()
        enableBluetooth()

        //----This enables Bluetooth Discoverability for 1 hour
        enableBluetoothDiscoverability()
        startBluetoothScan()
    }

    //------------Bluetooth Part-------------//
    private fun enableBluetoothDiscoverability() {
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600) // 3600 = 1 hour should make it not timeout
        }
        startActivity(discoverableIntent)
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
        val intent = Intent(this, BluetoothActivity::class.java)
        startActivity(intent)
    }

    companion object{
        var viewModel : RSSIViewModel? = null
        fun receiveRSSI(rssi : BluetoothRSSI){
            if (rssi != null){
                viewModel!!.addRSSI(rssi)
            }
        }
    }


    //----------Initialization of MainActivity UI----------//
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

}