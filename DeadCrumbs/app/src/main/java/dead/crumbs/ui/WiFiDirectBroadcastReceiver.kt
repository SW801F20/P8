package dead.crumbs.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast

class WiFiDirectBroadcastReceiver(val context: Context, val manager: WifiP2pManager, val channel: WifiP2pManager.Channel) : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        val myAction = intent.action
        if (myAction == WifiManager.RSSI_CHANGED_ACTION) {
            val rssi =  intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0)
            Toast.makeText(context,
                "WIFIDIRECT device discovered!  RSSI: $rssi", Toast.LENGTH_LONG).show()
        }
        when(intent.action) {
            WifiManager.EXTRA_NEW_RSSI->{
                val rssi = intent.getShortExtra(WifiManager.EXTRA_NEW_RSSI, Short.MIN_VALUE)
                Toast.makeText(context,
                    "WIFIDIRECT device discovered!  RSSI: $rssi", Toast.LENGTH_LONG).show()
            }
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION->{
                val rssi = intent.getShortExtra(WifiManager.EXTRA_NEW_RSSI, Short.MIN_VALUE)
                Toast.makeText(context,
                    "WIFIDIRECT device discovered!  RSSI: $rssi", Toast.LENGTH_LONG).show()
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {

                // Request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling activity is notified with a
                // callback on PeerListListener.onPeersAvailable()
                manager?.requestPeers(channel, peerListListener)


            }
            /*WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                activity.isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
            }*/
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {

                // The peer list has changed! We should probably do something about
                // that.

            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {

                // Connection state changed! We should probably do something about
                // that.

            }
            /*WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                (activity.supportFragmentManager.findFragmentById(R.id.frag_list) as DeviceListFragment)
                    .apply {
                        updateThisDevice(
                            intent.getParcelableExtra(
                                WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice
                        )
                    }
            }*/
        }
    }
}