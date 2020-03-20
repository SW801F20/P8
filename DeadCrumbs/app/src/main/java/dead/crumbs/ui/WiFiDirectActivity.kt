package dead.crumbs.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dead.crumbs.R


class WiFiDirectActivity(): AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wifi_direct_activity)
        WiFiDirectSetup()
    }

    private val intentFilter = IntentFilter()
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager


    private fun WiFiDirectSetup(){
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.EXTRA_NEW_RSSI)

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        registerReceiver(receiver, intentFilter)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)


    }

    public override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(this, manager, channel)
        registerReceiver(receiver, intentFilter)
    }

    public override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    private val peers = mutableListOf<WifiP2pDevice>()
    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)

            // If an AdapterView is backed by this data, notify it
            // of the change. For instance, if you have a ListView of
            // available peers, trigger an update.
            //(listAdapter as WiFiPeerListAdapter).notifyDataSetChanged()

            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.
        }
    }

    private fun manageNewRssi(rssi: Int){
        Toast.makeText(this@WiFiDirectActivity,
            "WIFIDIRECT device discovered!  RSSI: $rssi", Toast.LENGTH_LONG).show()
    }


    fun onPressDiscover(view: View){
        //Start discovery of wifi-direct devices
        //---Initiate Peer Discovery
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank. Code for peer discovery goes in the
                // onReceive method, detailed below.
            }

            override fun onFailure(reasonCode: Int) {
                throw Exception("WiFI Direct Discovery Failed!")
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
            }
        })
    }


}